package com.internship.rblp.handlers.student;

import com.internship.rblp.service.KycService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public enum GetStudentKycStatusHandler implements Handler<RoutingContext> {
    INSTANCE;

    private static KycService kycService;
    public static void init(KycService service){
        kycService = service;
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
