package com.internship.rblp.service;

import com.internship.rblp.models.entities.BulkUpload;
import com.internship.rblp.models.entities.BulkUploadError;
import com.internship.rblp.models.enums.BulkStatus;
import com.internship.rblp.repository.BulkUploadRepository;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class BulkUploadService {
    private static final Logger logger = LoggerFactory.getLogger(BulkUploadService.class);

    private final BulkUploadRepository bulkUploadRepository;
    private final AdminService adminService;
    private final Vertx vertx;


    public BulkUploadService(BulkUploadRepository bulkRepo,AdminService adminService,Vertx vertx){
        this.bulkUploadRepository = bulkRepo;
        this.adminService = adminService;
        this.vertx = vertx;
    }

    public Single<String> startBulkUpload(String filePath, String adminIdStr){
        return Single.fromCallable(() -> {

            BulkUpload upload = new BulkUpload();
            upload.setAdminId(UUID.fromString(adminIdStr));
            upload.setStatus(BulkStatus.IN_PROGRESS);
            upload.setCreatedAt(Instant.now());
            bulkUploadRepository.save(upload);

            processCsv(upload.getId(), filePath);

            return upload.getId().toString();
        }).subscribeOn(Schedulers.io());
    }

    public void processCsv(UUID uploadId, String filePath){
        vertx.fileSystem().readFile(filePath)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        buffer -> {
                            String content = buffer.toString();
                            String[] lines = content.split("\\r?\\n");

                            BulkUpload upload = bulkUploadRepository.findById(uploadId)
                                    .orElseThrow();

                            int total = 0;
                            int success = 0;
                            int fail = 0;

                            for(int i = 1; i<lines.length;i++){
                                String line = lines[i].trim();

                                if(line.isEmpty()) continue;

                                total++;
                                String[] cols = line.split(",");

                                if(cols.length<5){
                                    logError(upload, "Unknown", "Invalid CSV format (Missing columns)");
                                    fail++;
                                    continue;
                                }

                                String fullName = cols[0].trim();
                                String email = cols[1].trim();
                                String mobile = cols[2].trim();
                                String role = cols[3].trim();
                                String password = cols[4].trim();


                                if (email.isEmpty() || role.isEmpty()) {
                                    fail++;
                                    logError(upload, email.isEmpty() ? "Row " + (i+1) : email, "Missing Email or Role");
                                    continue;
                                }

                                JsonObject userJson = new JsonObject()
                                        .put("fullName", fullName)
                                        .put("email", email)
                                        .put("mobileNumber", mobile)
                                        .put("role", role)
                                        .put("password", password);

                                try{
                                    adminService.createUserSync(userJson);
                                    success++;
                                } catch(Exception e){
                                    fail++;
                                    String reason = e.getMessage();
                                    if(reason.length() > 250) reason = reason.substring(0,250);
                                    logError(upload,email,reason);
                                }
                            }

                            upload.setTotalRecords(total);
                            upload.setSuccessCount(success);
                            upload.setFailureCount(fail);

                            if(total == 0){
                                upload.setStatus(BulkStatus.FAILED);
                            } else if (fail == total){
                                upload.setStatus(BulkStatus.FAILED);
                            } else {
                                upload.setStatus(BulkStatus.COMPLETED);
                            }

                            upload.setCompletedAt(Instant.now());
                            bulkUploadRepository.save(upload);

                            vertx.fileSystem().delete(filePath).subscribe();
                        }, err -> {
                            logger.error("CSV read error", err);

                            BulkUpload upload = bulkUploadRepository.findById(uploadId)
                                    .orElse(null);
                            if(upload != null) {
                                upload.setStatus(BulkStatus.FAILED);
                                bulkUploadRepository.save(upload);
                            }
                        });
    }

    public void logError(BulkUpload upload, String email, String reason){
        BulkUploadError error = new BulkUploadError();
        error.setBulkUpload(upload);
        error.setEmail(email);
        error.setReason(reason);

        bulkUploadRepository.saveError(error);
    }

    public Single<JsonObject> getUploadStatus(String uploadIdStr) {
        return Single.fromCallable(() -> {
            UUID id = UUID.fromString(uploadIdStr);
            BulkUpload upload = bulkUploadRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Upload ID not found"));

            return new JsonObject()
                    .put("uploadId", upload.getId().toString())
                    .put("status", upload.getStatus())
                    .put("totalRecords", upload.getTotalRecords())
                    .put("successCount", upload.getSuccessCount())
                    .put("failureCount", upload.getFailureCount());
        }).subscribeOn(Schedulers.io());
    }

    public Single<JsonArray> getUploadErrors(String uploadIdStr) {
        return Single.fromCallable(() -> {
            UUID id = UUID.fromString(uploadIdStr);
            List<BulkUploadError> errors = bulkUploadRepository.findErrorsByUploadId(id);
            JsonArray arr = new JsonArray();
            for(BulkUploadError e : errors) {
                arr.add(new JsonObject().put("email", e.getEmail()).put("reason", e.getReason()));
            }
            return arr;
        }).subscribeOn(Schedulers.io());
    }
}
