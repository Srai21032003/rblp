package com.internship.rblp.handlers.kyc;

import com.internship.rblp.service.KycService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public enum ApproveKycHandler implements Handler<RoutingContext> {
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

        String kycIdStr = ctx.pathParam("kycId");

        if(kycIdStr == null){
            ctx.response().setStatusCode(400).end("KYS Id is required");
        }

        kycService.approveKyc(kycIdStr)
                .subscribe(
                        ignore ->{
                            ctx.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject()
                                            .put("message","Kyc Approved Successfully").encode());
                        },
                        err ->{
                            ctx.response().setStatusCode(500)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject()
                                            .put("error",err.getMessage()).encode());
                        }
                );
    }
}
