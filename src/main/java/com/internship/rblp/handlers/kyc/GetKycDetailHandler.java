package com.internship.rblp.handlers.kyc;

import com.internship.rblp.service.KycService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public enum GetKycDetailHandler implements Handler<RoutingContext> {
    INSTANCE;

    private static KycService kycService;

    public static void init(KycService service){
        kycService = service;
    }

    @Override
    public void handle(RoutingContext ctx){
        if(kycService == null){
            ctx.fail(500, new RuntimeException("KycService not intialized"));
            return;
        }

        String kycIdStr = ctx.pathParam("kycId");

        if(kycIdStr == null){
            ctx.response().setStatusCode(400).end(new JsonObject().put("error", "KYS Id is required").encode());
        }

        kycService.getKycDetail(kycIdStr)
                .subscribe(
                        result ->{
                            ctx.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type", "application/json")
                                    .end(result.encode());
                        },
                        err -> {
                            int statusCode = err.getMessage().contains("Id is required")?404:500;
                            ctx.response()
                                    .setStatusCode(statusCode)
                                    .putHeader("Content-Type","application/json")
                                    .end(new JsonObject().put("error", err.getMessage()).encode());
                        }
                );


    }
}
