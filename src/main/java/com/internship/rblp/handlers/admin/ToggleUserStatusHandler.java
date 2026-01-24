package com.internship.rblp.handlers.admin;


import com.internship.rblp.service.AdminService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ToggleUserStatusHandler implements Handler<RoutingContext> {
    INSTANCE;

    private static AdminService adminService;

    private static final Logger logger = LoggerFactory.getLogger(ToggleUserStatusHandler.class);

    public static void init(AdminService service){
        adminService = service;
    }

    @Override
    public void handle(RoutingContext ctx){
        if(adminService == null){
            ctx.fail(500, new RuntimeException("AdminService not initialized"));
            return;
        }


        String userId = ctx.pathParam("id");

        if(userId == null){
            ctx.response()
                    .setStatusCode(400).end(new JsonObject()
                            .put("error","User id required").encode());
            return;
        }

        adminService.toggleUserStatus(userId)
                .subscribe(
                        result -> {
                            logger.info("User status toggled successfully");
                            ctx.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type","application/json")
                                    .end(result.encode());
                        },
                        err -> {
                            logger.error("Error toggling user status: ", err);
                            int statusCode = err.getMessage().contains("not found")?404:500;
                            ctx.response()
                                    .setStatusCode(statusCode)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject().put("error", err.getMessage()).encode());
                        }
                );
    }
}
