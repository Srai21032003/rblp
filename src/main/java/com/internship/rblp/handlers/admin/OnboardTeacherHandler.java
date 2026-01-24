package com.internship.rblp.handlers.admin;

import com.internship.rblp.models.enums.Role;
import com.internship.rblp.service.AdminService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum OnboardTeacherHandler implements Handler<RoutingContext> {
    INSTANCE;

    private static AdminService adminService;
    private static final Logger logger = LoggerFactory.getLogger(OnboardTeacherHandler.class);

    public static void init(AdminService service){
        adminService = service;
    }

    @Override
    public void handle(RoutingContext ctx){
        if(adminService == null){
            ctx.fail(500, new RuntimeException("AdminService not intialized"));
            return;
        }
        JsonObject body = ctx.body().asJsonObject();

        if(body == null || !body.containsKey("email") || !body.containsKey("fullName")){
            ctx.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("error", "Email and Full Name are required").encode());
        }

        //force student role
        body.put("role", Role.TEACHER);

        if(!body.containsKey("password")){
            body.put("password", "Welcome123@");
        }

        adminService.onboardUser(

                body)
                .subscribe(
                        result -> {
                            logger.info("Teacher onboarded");
                            ctx.response()
                                    .setStatusCode(201)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject()
                                            .put("message","Teacher Onboarded sucessfully")
                                            .put("details",result)
                                            .encode());
                        },
                        err -> {
                            logger.error("Error onboarding teacher:",err);
                            int statusCode = err.getMessage().contains("exists")? 409 : 500;
                            ctx.response()
                                    .setStatusCode(statusCode)
                                    .putHeader("Content-Type","application/json")
                                    .end(new JsonObject().put("error",err.getMessage()).encode());
                        }
                );
    }
}
