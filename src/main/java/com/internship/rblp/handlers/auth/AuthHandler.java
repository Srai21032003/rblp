package com.internship.rblp.handlers.auth;

import com.internship.rblp.service.AuthService;
import com.internship.rblp.util.JwtUtil;
import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum AuthHandler implements Handler<RoutingContext> {

    LOGIN,
    SIGNUP,
    LOGOUT;

    private static final Logger logger = LoggerFactory.getLogger(AuthHandler.class);
    private static AuthService authService;

    // inject the service
    public static void init(AuthService service) {
        authService = service;
    }

    @Override
    public void handle(RoutingContext ctx) {
        if (authService == null) {
            throw new RuntimeException("AuthHandler not initialized! Call init() in MainVerticle.");
        }

        switch (this) {
            case LOGIN -> handleLogin(ctx);
            case SIGNUP -> handleSignup(ctx);
            case LOGOUT -> handleLogout(ctx);
        }
    }

    private void handleLogout(RoutingContext ctx) {
        Cookie cookie = Cookie.cookie("authToken", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        ctx.response().addCookie(cookie);
        ctx.response()
                .setStatusCode(200)
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject().put("message", "Logged out successfully").encode());
    }

    private void handleLogin(RoutingContext ctx) {

        Cookie existingToken = ctx.request().getCookie("authToken");

        if(existingToken != null){
            try{
                JwtUtil.validateToken(existingToken.getValue());
                ctx.response()
                        .setStatusCode(200)
                        .putHeader("Content-Type","application/json")
                        .end(new JsonObject().put("message","Already logged in").encode());
                return;
            } catch(Exception e){
                ctx.response().removeCookie("authToken");
            }
        }

        JsonObject body = ctx.body().asJsonObject();
        if (body == null || !body.containsKey("email") || !body.containsKey("password")) {
            ctx.response().setStatusCode(400).end(new JsonObject().put("error", "Email and password required").encode());
            return;
        }

        authService.login(body.getString("email"), body.getString("password"))
                .subscribe(
                        token -> {
                            logger.info("Login success:");
                            Cookie cookie = Cookie.cookie("authToken", token);
                            cookie.setPath("/");
                            cookie.setHttpOnly(true);
                            cookie.setSecure(false);  // to be set to "true" in production
                            cookie.setMaxAge(864000);
                            ctx.response().addCookie(cookie);
                            ctx.json(new JsonObject()
                                    .put("message", "Login successful")
                                    .put("token", token));
                        },
                        err -> {
                            int code = err.getMessage().contains("found") || err.getMessage().contains("credentials") ? 401 : 500;
                            ctx.response().setStatusCode(code).end(new JsonObject().put("error", err.getMessage()).encode());
                        }
                );
    }

    private void handleSignup(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        // Validation...
        if (body == null || !body.containsKey("email") || !body.containsKey("role")) {
            ctx.response().setStatusCode(400).end(new JsonObject().put("error", "Missing fields").encode());
            return;
        }

        authService.register(body)
                .subscribe(
                        token -> {
                            logger.info("Signup success");
                            ctx.response()
                                    .setStatusCode(201)
                                    .end(new JsonObject()
                                            .put("message", "User created")
                                            .put("token", token).encode());
                        },
                        err -> {
                            logger.error("Error during signup: ", err);
                            ctx.response()
                                    .setStatusCode(409)
                                    .end(new JsonObject()
                                            .put("error", err.getMessage())
                                            .encode());
                        }
                );
    }
}