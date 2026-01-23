package com.internship.rblp.handlers.kyc;

import com.internship.rblp.service.KycService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public enum RejectKycHandler implements Handler<RoutingContext> {
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
        JsonObject body = ctx.body().asJsonObject();

        if(body == null || !body.containsKey("reason")){
            ctx.response().setStatusCode(400)
                    .putHeader("Content-Type","application/json")
                    .end(new JsonObject()
                            .put("error","reason is required").encode());
            return;
        }

        String reason = body.getString("reason");

        if(kycIdStr == null){
            ctx.response().setStatusCode(400).end("KYS Id is required");
        }

        kycService.rejectKyc(kycIdStr, reason)
                .subscribe(
                        ignore ->{
                            ctx.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject()
                                            .put("message","Kyc Rejected").encode());
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
