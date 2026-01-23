package com.internship.rblp.handlers.kyc;

import com.internship.rblp.service.KycService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

public enum GetAllKycHandler implements Handler<RoutingContext> {
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

        kycService.getAllSubmissions()
                .subscribe(
                        result ->{
                            ctx.response()
                                    .setStatusCode(200)
                                    .end(result.encode());
                        },
                        err -> {
                            ctx.response()
                                    .setStatusCode(500)
                                    .putHeader("Content-Type","application/json")
                                    .end(new JsonArray().add(err.getMessage()).encode());
                        }
                );


    }
}
