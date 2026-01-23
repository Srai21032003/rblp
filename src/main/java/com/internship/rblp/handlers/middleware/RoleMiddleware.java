package com.internship.rblp.handlers.middleware;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public enum RoleMiddleware implements Handler<RoutingContext> {
    ADMIN,
    STUDENT,
    TEACHER;

    @Override
    public void handle(RoutingContext ctx){
        String userRole = ctx.get("role");

        if(userRole == null){
            ctx.response()
                    .setStatusCode(401)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("error","Unauthorized: Role information missing").encode());
            return;
        }

        if (!this.name().equalsIgnoreCase(userRole)) {
            ctx.response().setStatusCode(403) // 403 Forbidden
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("error", "Access Denied: You must be a " + this.name()).encode());
            return;
        }
        ctx.next();

    }
}
