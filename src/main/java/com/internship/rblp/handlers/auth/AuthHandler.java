package com.internship.rblp.handlers.auth;

import com.internship.rblp.service.AuthService;
import com.internship.rblp.models.enums.Role;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum AuthHandler implements Handler<RoutingContext> {

    // The Enum Constants act as the specific handlers
    LOGIN,
    SIGNUP;

    private static final Logger logger = LoggerFactory.getLogger(AuthHandler.class);
    private static AuthService authService;

    // 1. Static Init Method to inject the service
    public static void init(AuthService service) {
        authService = service;
    }

    // 2. The main handle method dispatches based on "this"
    @Override
    public void handle(RoutingContext ctx) {
        if (authService == null) {
            throw new RuntimeException("AuthHandler not initialized! Call init() in MainVerticle.");
        }

        switch (this) {
            case LOGIN -> handleLogin(ctx);
            case SIGNUP -> handleSignup(ctx);
        }
    }

    // --- Logic for Login ---
    private void handleLogin(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        if (body == null || !body.containsKey("email") || !body.containsKey("password")) {
            ctx.response().setStatusCode(400).end(new JsonObject().put("error", "Email and password required").encode());
            return;
        }

        authService.login(body.getString("email"), body.getString("password"))
                .subscribe(
                        token -> {
                            logger.info("Login success: {}", body.getString("email"));
                            ctx.json(new JsonObject().put("token", token));
                        },
                        err -> {
                            int code = err.getMessage().contains("found") || err.getMessage().contains("credentials") ? 401 : 500;
                            ctx.response().setStatusCode(code).end(new JsonObject().put("error", err.getMessage()).encode());
                        }
                );
    }

    // --- Logic for Signup ---
    private void handleSignup(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        // Validation...
        if (body == null || !body.containsKey("email") || !body.containsKey("role")) {
            ctx.response().setStatusCode(400).end(new JsonObject().put("error", "Missing fields").encode());
            return;
        }

        authService.register(body)
                .subscribe(
                        token -> ctx.response().setStatusCode(201).end(new JsonObject().put("token", token).encode()),
                        err -> ctx.response().setStatusCode(409).end(new JsonObject().put("error", err.getMessage()).encode())
                );
    }
}