package com.internship.rblp.handlers.teacher;

import com.internship.rblp.service.TeacherService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public enum UpdateTeacherProfileHandler implements Handler<RoutingContext> {

    INSTANCE;

    private static TeacherService teacherService;

    public static void init(TeacherService service) {
        teacherService = service;
    }

    @Override
    public void handle(RoutingContext ctx) {
        if (teacherService == null) {
            ctx.fail(500, new RuntimeException("TeacherService not initialized"));
            return;
        }

        String userId = ctx.get("userId");
        JsonObject body = ctx.body().asJsonObject();

        if (body == null || body.isEmpty()) {
            ctx.response().setStatusCode(400)
                    .end(new JsonObject().put("error", "Request body is required").encode());
            return;
        }

        teacherService.updateProfile(userId, body)
                .subscribe(
                        updatedJson -> {
                            ctx.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject()
                                            .put("message", "Teacher profile updated")
                                            .put("data", updatedJson)
                                            .encode()
                                    );
                        },
                        err -> {
                            int statusCode = err.getMessage().contains("not found") ? 404 : 500;
                            ctx.response()
                                    .setStatusCode(statusCode)
                                    .end(new JsonObject().put("error", err.getMessage()).encode());
                        }
                );
    }
}