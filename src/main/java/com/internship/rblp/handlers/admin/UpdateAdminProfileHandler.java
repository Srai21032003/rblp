package com.internship.rblp.handlers.admin;

import com.internship.rblp.service.AdminService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum UpdateAdminProfileHandler implements Handler<RoutingContext> {

    INSTANCE;

    private static AdminService adminService;
    private static final Logger logger = LoggerFactory.getLogger(UpdateAdminProfileHandler.class);

    public static void init(AdminService service) {
        adminService = service;
    }

    @Override
    public void handle(RoutingContext ctx) {
        if (adminService == null) {
            ctx.fail(500, new RuntimeException("AdminService not initialized"));
            return;
        }

        String userId = ctx.get("userId");
        JsonObject body = ctx.body().asJsonObject();

        if (body == null || body.isEmpty()) {
            ctx.response().setStatusCode(400)
                    .end(new JsonObject().put("error", "Request body is required").encode());
            return;
        }

        adminService.updateAdminProfile(userId, body)
                .subscribe(
                        updatedJson -> {
                            logger.info("Admin profile updated");
                            ctx.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject()
                                            .put("message", "Admin profile updated")
                                            .put("data", updatedJson)
                                            .encode()
                                    );
                        },
                        err -> {
                            logger.error("Error updating admin profile: ", err);
                            ctx.response()
                                    .setStatusCode(500)
                                    .end(new JsonObject().put("error", err.getMessage()).encode());
                        }
                );
    }
}