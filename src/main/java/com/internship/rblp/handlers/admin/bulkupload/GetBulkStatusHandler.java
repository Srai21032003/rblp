package com.internship.rblp.handlers.admin.bulkupload;

import com.internship.rblp.service.BulkUploadService;
import io.vertx.core.Handler;
import io.vertx.rxjava3.ext.web.RoutingContext;

public enum GetBulkStatusHandler implements Handler<RoutingContext> {
    INSTANCE;

    private static BulkUploadService service;
    public static void init(BulkUploadService s) { service = s; }

    @Override
    public void handle(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        service.getUploadStatus(id).subscribe(
                json -> {
                    ctx.response()
                            .putHeader("Content-Type","application/json")
                            .end(json.encode());
                },
                err -> {
                    ctx.fail(err.getMessage()
                            .contains("not found") ? 404 : 500, err);
                }
        );
    }
}