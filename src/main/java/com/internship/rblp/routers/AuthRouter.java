package com.internship.rblp.routers;

import com.internship.rblp.handlers.auth.AuthHandler;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;

public enum AuthRouter {

    INSTANCE;

    public Router create(Vertx vertx) {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        // routes
        router.post("/login").handler(AuthHandler.LOGIN);
        router.post("/signup").handler(AuthHandler.SIGNUP);
        router.post("/logout").handler(AuthHandler.LOGOUT);

        return router;
    }
}