package com.internship.rblp.routers;

import com.internship.rblp.handlers.middleware.JwtAuthMiddleware;
import com.internship.rblp.handlers.middleware.RoleMiddleware;
import com.internship.rblp.handlers.student.SubmitStudentKycHandler;
import com.internship.rblp.handlers.student.UpdateStudentProfileHandler;
import com.internship.rblp.handlers.student.GetStudentKycStatusHandler;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;

public enum StudentRouter {

    INSTANCE;

    public Router create(Vertx vertx) {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.route().handler(JwtAuthMiddleware.INSTANCE);
        router.route().handler(RoleMiddleware.STUDENT);

        //routes
        router.put("/profile").handler(UpdateStudentProfileHandler.INSTANCE);
        router.post("/kyc").handler(SubmitStudentKycHandler.INSTANCE);
        router.get("/kyc/status").handler(GetStudentKycStatusHandler.INSTANCE);

        return router;
    }
}