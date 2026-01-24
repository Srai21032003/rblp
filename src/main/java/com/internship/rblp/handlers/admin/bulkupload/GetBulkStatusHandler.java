package com.internship.rblp.handlers.admin.bulkupload;

import com.internship.rblp.service.BulkUploadService;
import io.vertx.core.Handler;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum GetBulkStatusHandler implements Handler<RoutingContext> {
    INSTANCE;

    private static BulkUploadService service;
    public static void init(BulkUploadService s) { service = s; }
    private static final Logger logger = LoggerFactory.getLogger(GetBulkStatusHandler.class);

    @Override
    public void handle(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        service.getUploadStatus(id).subscribe(
                json -> {
                    logger.info("Got the bulk upload status");
                    ctx.response()
                            .putHeader("Content-Type","application/json")
                            .end(json.encode());
                },
                err -> {
                    logger.error("Error retrieving bulk upload status: ", err);
                    ctx.fail(err.getMessage()
                            .contains("not found") ? 404 : 500, err);
                }
        );
    }
}