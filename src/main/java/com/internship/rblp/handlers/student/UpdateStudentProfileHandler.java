package com.internship.rblp.handlers.student;

import com.internship.rblp.service.AuditLogsService;
import com.internship.rblp.service.StudentService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public enum UpdateStudentProfileHandler implements Handler<RoutingContext> {

    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(UpdateStudentProfileHandler.class);
    private static StudentService studentService;
    private static AuditLogsService auditService;

    public static void init(StudentService service, AuditLogsService audService) {

        studentService = service;
        auditService = audService;
    }

    @Override
    public void handle(RoutingContext ctx) {
        if (studentService == null) {
            ctx.fail(500, new RuntimeException("StudentService not initialized"));
            return;
        }

        String userId = ctx.get("userId");
        JsonObject body = ctx.body().asJsonObject();

        if (body == null || body.isEmpty()) {
            ctx.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("error", "Request body is required").encode());
            return;
        }

        // Call Service
        studentService.updateProfile(userId, body)
                .subscribe(
                        updatedJson -> {
                            ctx.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject()
                                            .put("message", "Student profile updated")
                                            .put("data", updatedJson)
                                            .encode()
                                    );
                            String updateProfileSuccess = "UPDATED PROFILE AT "+ auditService.getCurrentTimestamp();
                            auditService.addAuditLogEntry(ctx, updateProfileSuccess)
                                    .subscribe(
                                            ()-> logger.info("Audit log entry added successfully"),
                                            err -> {
                                                logger.error("Failed to add audit log entry for updateProfile", err);
                                            }
                                    );
                        },
                        err -> {
                            int statusCode = err.getMessage().contains("not found") ? 404 : 500;
                            ctx.response()
                                    .setStatusCode(statusCode)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject().put("error", err.getMessage()).encode());
                        }
                );
    }
}