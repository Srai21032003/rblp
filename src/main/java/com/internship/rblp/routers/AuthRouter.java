package com.internship.rblp.routers;

import com.internship.rblp.handlers.auth.LoginHandler;
import com.internship.rblp.handlers.auth.SignupHandler;
import com.internship.rblp.service.AuthService;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;

public enum AuthRouter {

    INSTANCE;
    public static Router create(Vertx vertx){
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        AuthService authService = new AuthService();
        LoginHandler loginHandler = new LoginHandler(authService);
        SignupHandler signupHandler = new SignupHandler(authService);

        router.post("/login").handler(loginHandler);
        router.post("/signup").handler(signupHandler);

        return router;
    }
}
