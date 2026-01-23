package com.internship.rblp.handlers.admin.bulkupload;

import com.internship.rblp.service.BulkUploadService;
import io.vertx.core.Handler;
import io.vertx.rxjava3.ext.web.RoutingContext;

public enum GetBulkErrorsHandler implements Handler<RoutingContext> {
    INSTANCE;

    private static BulkUploadService service;
    public static void init(BulkUploadService s) { service = s; }

    @Override
    public void handle(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        service.getUploadErrors(id).subscribe(
                array -> {
                    ctx.response()
                            .putHeader("Content-Type","application/json")
                            .end(array.encode());
                },
                err -> {
                    ctx.fail(err.getMessage()
                            .contains("not found") ? 404 : 500, err);
                }
        );
    }
}