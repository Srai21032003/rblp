package com.internship.rblp.handlers.student;

import com.internship.rblp.service.KycService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.FileUpload;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.List;

public enum SubmitStudentKycHandler implements Handler<RoutingContext> {
    INSTANCE;

    private static KycService kycService;

    public static void init(KycService service){
        kycService = service;
    }
    @Override
    public void handle(RoutingContext ctx){

        if(kycService == null){
            ctx.fail(500, new RuntimeException("KycService not initialized"));
            return;
        }

        String userId = ctx.get("userId");
        List<FileUpload> uploads = ctx.fileUploads();

        if(uploads.size() < 3){
            ctx.response().setStatusCode(400)
                    .end(new JsonObject()
                            .put("error","Please upload all 3 documents")
                            .encode());
            return;
        }

        String address = ctx.request().getFormAttribute("address");
        String dob = ctx.request().getFormAttribute("dob");

        JsonArray docsArray = new JsonArray();

        for (FileUpload f : uploads) {
            JsonObject doc = new JsonObject();
            doc.put("filePath", f.uploadedFileName());
            doc.put("originalFileName",f.fileName());

            if (f.name().equals("panFile")) {
                doc.put("docType", "PAN");
                doc.put("documentNumber", ctx.request().getFormAttribute("panNumber"));
                doc.put("nameOnDoc", ctx.request().getFormAttribute("nameOnPan"));
            } else if (f.name().equals("aadhaarFile")) {
                doc.put("docType", "AADHAAR");
                doc.put("documentNumber", ctx.request().getFormAttribute("aadhaarNumber"));
                doc.put("nameOnDoc", ctx.request().getFormAttribute("nameOnAadhaar"));
            } else if (f.name().equals("passportFile")) {
                doc.put("docType", "PASSPORT");
                doc.put("documentNumber", ctx.request().getFormAttribute("passportNumber"));
                doc.put("nameOnDoc", ctx.request().getFormAttribute("nameOnPassport"));
            }
            docsArray.add(doc);
        }

        JsonObject serviceData = new JsonObject()
                .put("address", address)
                .put("dob", dob)
                .put("documents", docsArray);

        kycService.submitKyc(userId,serviceData)
                .subscribe(
                        kycId -> {
                            ctx.response()
                                    .setStatusCode(201)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject()
                                            .put("message", "Student KYC submitted successfully")
                                            .put("kycId", kycId).encode());
                        },
                        err -> {
                            uploads.forEach(f -> ctx.vertx().fileSystem().delete(f.uploadedFileName()));

                            int statusCode = err.getMessage().contains("already submitted") ? 409 : 500;
                            ctx.response().setStatusCode(statusCode)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject().put("error", err.getMessage()).encode());
                        }
                );
    }
}
