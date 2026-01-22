//package com.internship.rblp.routers;
//
//import com.internship.rblp.handlers.auth.AuthHandler;
//import com.internship.rblp.handlers.auth.LoginHandler;
//import com.internship.rblp.handlers.auth.SignupHandler;
//import com.internship.rblp.service.AuthService;
//import io.vertx.rxjava3.core.Vertx;
//import io.vertx.rxjava3.ext.web.Router;
//import io.vertx.rxjava3.ext.web.handler.BodyHandler;
//
//public enum AuthRouter {
//
//    INSTANCE;
//    public static Router create(Vertx vertx){
//        Router router = Router.router(vertx);
//
//        router.route().handler(BodyHandler.create());
//
//        AuthService authService = new AuthService();
//
//        router.post("/login").handler(loginHandler);
//        router.post("/signup").handler(signupHandler);
//
//        return router;
//    }
//}
package com.internship.rblp.routers;

import com.internship.rblp.handlers.auth.AuthHandler;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;

public enum AuthRouter {

    INSTANCE;

    public Router create(Vertx vertx) {
        Router router = Router.router(vertx);

        // Enable reading of Request Body (JSON)
        router.route().handler(BodyHandler.create());

        // Define Routes using Enum Constants
        router.post("/login").handler(AuthHandler.LOGIN);
        router.post("/signup").handler(AuthHandler.SIGNUP);

        return router;
    }
}