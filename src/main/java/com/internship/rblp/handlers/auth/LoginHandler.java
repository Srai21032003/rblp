package com.internship.rblp.handlers.auth;

import com.internship.rblp.service.AuthService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoginHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);
    private final AuthService authService;

    public LoginHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(RoutingContext ctx){
        JsonObject body = ctx.body().asJsonObject();

        if(body == null || !body.containsKey("email") || !body.containsKey("password")){
            ctx.response().setStatusCode(400).end(new JsonObject().put("error","Email and password are required").encode());
            return ;
        }

        String email = body.getString("email");
        String password = body.getString("password");

        authService.login(email,password)
                .subscribe(
                        token -> {
                            logger.info("User logged in successfully: {}", email);
                            ctx.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type","application/json")
                                    .end(new JsonObject()
                                            .put("token",token)
                                            .put("message","Login Successful")
                                            .encode()
                                    );
                        },
                        err -> {
                            logger.warn("login failed for {} : {}", email, err.getMessage());

                            int statusCode = err.getMessage().equals("User not found") || err.getMessage().equals("Invalid Credentials")
                                    ? 401 : 500;

                            ctx.response()
                                    .setStatusCode(statusCode)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject().put("error",err.getMessage()).encode());
                        }
                );
    }
}
