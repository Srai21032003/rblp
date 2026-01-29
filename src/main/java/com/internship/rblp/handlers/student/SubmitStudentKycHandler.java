package com.internship.rblp.handlers.student;

import com.internship.rblp.service.AuditLogsService;
import com.internship.rblp.service.FileStorageService;
import com.internship.rblp.service.KycService;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.FileUpload;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

public enum SubmitStudentKycHandler implements Handler<RoutingContext> {
    INSTANCE;

    private static Logger logger = LoggerFactory.getLogger(SubmitStudentKycHandler.class);
    private static KycService kycService;
    private static FileStorageService fileStorageService;
    private static AuditLogsService auditService;

    public static void init(KycService service, FileStorageService storageService, AuditLogsService audService) {
        kycService = service;
        fileStorageService = storageService;
        auditService = audService;
    }

    @Override
    public void handle(RoutingContext ctx) {
        if (kycService == null) {
            ctx.fail(500, new RuntimeException("KycService not initialized"));
            return;
        }

        String userId = ctx.get("userId");
        List<FileUpload> uploads = ctx.fileUploads();

        if (uploads.size() < 3) {
            ctx.response().setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("error", "Please upload all 3 documents (PAN, Aadhaar, Passport)").encode());
            return;
        }

        Observable.fromIterable(uploads)
                .flatMapSingle(file ->
                        fileStorageService.saveFile(file, userId)
                                .map(savedPath ->{
                                    JsonObject doc = new JsonObject();
                                    doc.put("savedPath", savedPath);
                                    doc.put("originalFileName", file.fileName());
                                    doc.put("formFieldName", file.name());
                                    return doc;
                                })
                )
                .toList()
                .subscribe(
                        processedFiles-> {
                            try{
                                JsonObject serviceData = buildServicePayload(ctx,processedFiles);
                                String saveFileSuccess = "FILE SAVED TO LOCAL AT "+ auditService.getCurrentTimestamp();
                                auditService.addAuditLogEntry(ctx, saveFileSuccess)
                                        .subscribe(
                                                ()-> logger.info("Audit log entry added successfully"),
                                                err -> {
                                                    logger.error("Failed to add audit log entry for saveFile", err);
                                                }
                                        );

                                kycService.submitKyc(userId, serviceData, ctx)
                                        .subscribe(
                                                kycId -> {
                                                    ctx.response().setStatusCode(200)
                                                            .putHeader("Content-Type", "application/json")
                                                            .end(new JsonObject()
                                                                    .put("message", "Student KYC submitted successfully")
                                                                    .put("kycId", kycId).encode());
                                                    String submitKycSuccess = "SUBMITTED KYC AT "+ auditService.getCurrentTimestamp();
                                                    auditService.addAuditLogEntry(ctx, submitKycSuccess)
                                                            .subscribe(
                                                                    ()-> logger.info("Audit log entry added successfully"),
                                                                    err -> {
                                                                        logger.error("Failed to add audit log entry for submitKyc", err);
                                                                    }
                                                            );
                                                },
                                                err -> {
                                                    processedFiles.forEach(f -> ctx.vertx().fileSystem().delete(f.getString("savedPath")));

                                                    int statusCode = err.getMessage().contains("already submitted") ? 409 : 500;
                                                    ctx.response().setStatusCode(statusCode)
                                                            .putHeader("Content-Type", "application/json")
                                                            .end(new JsonObject().put("error", err.getMessage()).encode());
                                                });
                            } catch(Exception e){
                                ctx.fail(400, e);
                            }
                        }, err->{
                            ctx.fail(500, new RuntimeException("Failed to store docs"+ err.getMessage()));
                        }
                );
    }

    private JsonObject buildServicePayload(RoutingContext ctx, @NonNull List<JsonObject> processedFiles) {

        JsonArray docsArray = new JsonArray();

        for (JsonObject f: processedFiles) {
            JsonObject doc = new JsonObject();

            doc.put("filePath", f.getString("savedPath"));
            doc.put("originalFileName",f.getString("originalFileName"));

            String fieldName = f.getString("formFieldName");

            if ("panFile".equals(fieldName)) {
                doc.put("docType", "PAN");
                doc.put("documentNumber", ctx.request().getFormAttribute("panNumber"));
                doc.put("nameOnDoc", ctx.request().getFormAttribute("nameOnPan"));
            } else if ("aadhaarFile".equals(fieldName)) {
                doc.put("docType", "AADHAAR");
                doc.put("documentNumber", ctx.request().getFormAttribute("aadhaarNumber"));
                doc.put("nameOnDoc", ctx.request().getFormAttribute("nameOnAadhaar"));
            } else if ("passportFile".equals(fieldName)) {
                doc.put("docType", "PASSPORT");
                doc.put("documentNumber", ctx.request().getFormAttribute("passportNumber"));
                doc.put("nameOnDoc", ctx.request().getFormAttribute("nameOnPassport"));
            }
            docsArray.add(doc);
        }

        return new JsonObject()
                .put("address", ctx.request().getFormAttribute("address"))
                .put("dob", ctx.request().getFormAttribute("dob"))
                .put("documents", docsArray);
    }
}