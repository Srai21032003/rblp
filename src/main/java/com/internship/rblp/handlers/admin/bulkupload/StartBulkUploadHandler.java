package com.internship.rblp.handlers.admin.bulkupload;

import com.internship.rblp.service.BulkUploadService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.FileUpload;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.List;

public enum StartBulkUploadHandler implements Handler<RoutingContext> {
    INSTANCE;

    private static BulkUploadService service;

    public static void init(BulkUploadService s) { service = s; }

    @Override
    public void handle(RoutingContext ctx) {
        if (service == null) {
            ctx.fail(500, new RuntimeException("Service not initialized"));
            return;
        }

        String adminId = ctx.get("userId");
        List<FileUpload> uploads = ctx.fileUploads();

        if (uploads == null || uploads.isEmpty()) {
            ctx.response().setStatusCode(400).end("CSV file is required");
            return;
        }

        FileUpload file = uploads.iterator().next();

        if (!file.fileName().endsWith(".csv")) {
            ctx.response().setStatusCode(400).end("Only .csv files are allowed");
            return;
        }

        service.startBulkUpload(file.uploadedFileName(), adminId)
                .subscribe(
                        uploadId -> {
                            ctx.response().setStatusCode(202)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject()
                                            .put("message", "Upload initiated")
                                            .put("uploadId", uploadId)
                                            .encode());
                        },
                        err -> ctx.fail(500, err)
                );
    }
}