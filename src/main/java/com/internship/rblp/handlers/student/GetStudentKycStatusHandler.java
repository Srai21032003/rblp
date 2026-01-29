package com.internship.rblp.handlers.student;

import com.internship.rblp.models.enums.AuditAction;
import com.internship.rblp.service.AuditLogsService;
import com.internship.rblp.service.KycService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public enum GetStudentKycStatusHandler implements Handler<RoutingContext> {
    INSTANCE;

    private static AuditLogsService auditService;
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
                            auditService.logSuccess(ctx, AuditAction.FETCH_KYC_STATUS).subscribe();
                        },
                        err -> {
                            int statusCode = 500;
                            if(err.getMessage().contains("not found")){
                                statusCode = 404;
                            }
                            auditService.logFailure(ctx, AuditAction.FETCH_KYC_STATUS, err.getMessage()).subscribe();
                            ctx.response()
                                    .setStatusCode(statusCode)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject().put("error", err.getMessage()).encode());
                        }
                );
    }
}
