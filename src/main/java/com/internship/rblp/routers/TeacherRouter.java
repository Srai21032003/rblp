package com.internship.rblp.routers;

import com.internship.rblp.handlers.middleware.JwtAuthMiddleware;
import com.internship.rblp.handlers.student.SubmitTeacherKycHandler;
import com.internship.rblp.handlers.teacher.UpdateTeacherProfileHandler;
import com.internship.rblp.handlers.teacher.GetTeacherKycStatusHandler;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;

public enum TeacherRouter {

    INSTANCE;

    public Router create(Vertx vertx) {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.route().handler(JwtAuthMiddleware.INSTANCE);

        //routes
        router.put("/profile").handler(UpdateTeacherProfileHandler.INSTANCE);
        router.post("/kyc").handler(SubmitTeacherKycHandler.INSTANCE);
        router.get("/kyc/status").handler(GetTeacherKycStatusHandler.INSTANCE);

        return router;
    }
}