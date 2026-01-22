package com.internship.rblp.handlers.auth;

import com.internship.rblp.models.enums.Role;
import com.internship.rblp.service.AuthService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignupHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(SignupHandler.class);
    private final AuthService authService;

    public SignupHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(RoutingContext ctx){
        JsonObject body = ctx.body().asJsonObject();

        if(body == null || !body.containsKey("email") || !body.containsKey("password") || !body.containsKey("fullName") || !body.containsKey("role")){
            ctx.response().setStatusCode(400).end(new JsonObject().put("error","Missing required fields").encode());
            return;
        }

        try{
            Role.valueOf(body.getString("role").toUpperCase());

        } catch(IllegalArgumentException e){
            ctx.response().setStatusCode(400).end(new JsonObject().put("error","Invalid Role. Use STUDENT or TEACHER").encode());
            return;
        }

        authService.register(body)
                .subscribe(
                        token -> {
                            logger.info("New User registered : {}", body.getString("email"));
                            ctx.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type","application/json")
                                    .end(new JsonObject()
                                            .put("message","User Registered Successfully")
                                            .put("token",token).encode());
                        },
                        err -> {
                            logger.error("SignUp failed",err);
                            int code = err.getMessage().equals("Email already registered") ? 409:500;
                            ctx.response()
                                    .setStatusCode(code)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject().put("error",err.getMessage()).encode());
                        }
                );
    }

}
