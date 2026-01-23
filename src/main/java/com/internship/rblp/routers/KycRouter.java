package com.internship.rblp.routers;

import com.internship.rblp.handlers.kyc.ApproveKycHandler;
import com.internship.rblp.handlers.kyc.GetAllKycHandler;
import com.internship.rblp.handlers.kyc.GetKycDetailHandler;
import com.internship.rblp.handlers.kyc.RejectKycHandler;
import com.internship.rblp.handlers.middleware.JwtAuthMiddleware;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;

public enum KycRouter {
    INSTANCE;
    public Router create(Vertx vertx){
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.route().handler(JwtAuthMiddleware.INSTANCE);

        router.get("/").handler(GetAllKycHandler.INSTANCE);
        router.get("/:kycId").handler(GetKycDetailHandler.INSTANCE);
        router.post("/:kycId/approve").handler(ApproveKycHandler.INSTANCE);
        router.post("/:kycId/reject").handler(RejectKycHandler.INSTANCE);

        return router;
    }
}
