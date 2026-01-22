package com.internship.rblp.routers;

import com.internship.rblp.handlers.middleware.JwtAuthMiddleware;
import com.internship.rblp.handlers.user.UserHandler;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;

public enum UserRouter {

    INSTANCE;
    public static Router create(Vertx vertx) {
        Router router = Router.router(vertx);

        router.route().handler(JwtAuthMiddleware.INSTANCE);

        router.route().handler(UserHandler.GET_PROFILE);

        return router;
    }
}