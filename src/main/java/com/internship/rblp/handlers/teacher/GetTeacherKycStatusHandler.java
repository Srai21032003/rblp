package com.internship.rblp.handlers.teacher;

import com.internship.rblp.service.AuditLogsService;
import com.internship.rblp.service.KycService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public enum GetTeacherKycStatusHandler implements Handler<RoutingContext> {
    INSTANCE;

    private static AuditLogsService auditService;
    private static Logger logger = LoggerFactory.getLogger(GetTeacherKycStatusHandler.class);
    private static KycService kycService;
    public static void init(KycService service, AuditLogsService audService){
        kycService = service;
        auditService = audService;
    }

    @Override
    public void handle(RoutingContext ctx){

        if(kycService == null) {
            ctx.fail(500,new RuntimeException("KycService not intitialized"));
            return;
        }

        String userId = ctx.get("userId");
        if(userId == null){
            ctx.response().setStatusCode(401).end(new JsonObject().put("error", "Unauthorized").encode());
            return;
        }

        kycService.getKycStatus(userId)
                .subscribe(
                        statusJson -> {
                            ctx.response()
                                    .setStatusCode(201)
                                    .putHeader("Content-Type", "application/json")
                                    .end(statusJson.encode());
                            String getKycStatusSuccess = "FETCHED KYC STATUS AT "+ auditService.getCurrentTimestamp();
                            auditService.addAuditLogEntry(ctx, getKycStatusSuccess)
                                    .subscribe(
                                            ()-> logger.info("Audit log entry added successfully"),
                                            err -> {
                                                logger.error("Failed to add audit log entry for getKycStatus", err);
                                            }
                                    );
                        },
                        err -> {
                            int statusCode = 500;
                            if(err.getMessage().contains("not found")){
                                statusCode = 404;
                            }

                            ctx.response()
                                    .setStatusCode(statusCode)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject().put("error", err.getMessage()).encode());
                        }
                );
    }
}

