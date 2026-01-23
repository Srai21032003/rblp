package com.internship.rblp.handlers.student;

import com.internship.rblp.service.KycService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public enum SubmitStudentKycHandler implements Handler<RoutingContext> {
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

        String userId = ctx.get("userId");
        JsonObject body = ctx.body().asJsonObject();

        if(body == null || body.isEmpty()){
            ctx.response().setStatusCode(400)
                    .putHeader("Content-Type","application/json")
                    .end(new JsonObject().put("error", "Request body is required").encode());
            return;
        }

        kycService.submitKyc(userId,body)
                .subscribe(
                        kycId -> {
                            ctx.response()
                                    .setStatusCode(201)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject()
                                            .put("message", "Student KYC submitted successfully")
                                            .put("kycId", kycId).encode());
                        },
                        err -> {
                            int statusCode = 500;

                            String msg = err.getMessage();

                            if(msg.contains("already submitted")){
                                statusCode = 409;
                            } else if(msg.contains("required")){
                                statusCode = 400;
                            }
                            ctx.response()
                                    .setStatusCode(statusCode)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject().put("error", msg).encode());
                        }
                );
    }
}
