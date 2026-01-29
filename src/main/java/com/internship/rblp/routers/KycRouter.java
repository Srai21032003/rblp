package com.internship.rblp.routers;

import com.internship.rblp.handlers.kyc.*;
import com.internship.rblp.handlers.middleware.JwtAuthMiddleware;
import com.internship.rblp.handlers.middleware.RoleMiddleware;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;

public enum KycRouter {
    INSTANCE;
    public Router create(Vertx vertx){
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.route().handler(JwtAuthMiddleware.INSTANCE);
        router.route().handler(RoleMiddleware.ADMIN);

        router.get("/").handler(GetAllKycHandler.INSTANCE);
        router.get("/:kycId").handler(GetKycDetailHandler.INSTANCE);
        router.get("/:kycId/ai").handler(GetKycWithAiDetailsHandler.INSTANCE);
        router.post("/:kycId/approve").handler(ApproveKycHandler.INSTANCE);
        router.post("/:kycId/reject").handler(RejectKycHandler.INSTANCE);

        return router;
    }
}
