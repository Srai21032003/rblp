package com.internship.rblp.handlers.admin;

import com.internship.rblp.service.AdminService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum GetUserListHandler implements Handler<RoutingContext> {
    INSTANCE;
    private static AdminService adminService;

    private static final Logger logger = LoggerFactory.getLogger(GetUserListHandler.class);
    public static void init(AdminService service){
        adminService = service;
    }

    @Override
    public void handle(RoutingContext ctx) {
        if(adminService == null){
            ctx.fail(500, new RuntimeException("AdminService not intialized"));
            return;
        }

        adminService.getAllUsers()
                .subscribe(
                       users -> {
                           logger.info("Retrieved user list");
                           JsonArray userList = new JsonArray();
                           users.forEach(u -> userList.add(new JsonObject()
                                   .put("userId",u.getUserId().toString())
                                   .put("email",u.getEmail())
                                   .put("fullName",u.getFullName())
                                   .put("role",u.getRole().toString())
                                   .put("isActive",u.getIsActive())));
                           ctx.response().setStatusCode(200)
                                   .putHeader("Content-Type","application/json")
                                   .end(userList.encode());
                       },
                        err -> {
                           logger.error("Error retrieving user list: ", err);
                           ctx.fail(500,err);
                        }
                );
    }
}
