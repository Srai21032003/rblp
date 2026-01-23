package com.internship.rblp.handlers.user;

import com.internship.rblp.models.entities.User;
import com.internship.rblp.models.enums.Role;
import io.ebean.DB;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import java.util.UUID;

public enum GetProfileHandler implements Handler<RoutingContext> {

    GET_PROFILE;

    // No service needed for this simple DB fetch, so no init() required strictly,
    // but if you add complex logic later, add an init() method.

    @Override
    public void handle(RoutingContext ctx) {
        if (this == GET_PROFILE) {
            handleGetProfile(ctx);
        }
    }

    private void handleGetProfile(RoutingContext ctx) {
        String userIdStr = ctx.get("userId"); // Set by Middleware
        if (userIdStr == null) {
            ctx.fail(401);
            return;
        }

        UUID userId = UUID.fromString(userIdStr);
        User user = DB.find(User.class)
                .fetch("studentProfile") // Eagerly load student data
                .fetch("teacherProfile") // Eagerly load teacher data
                .setId(userId)
                .findOne();

        if (user == null) {
            ctx.response().setStatusCode(404).end(new JsonObject().put("error", "User not found").encode());
            return;
        }

        // 2. Build Base Response
        JsonObject response = new JsonObject()
                .put("userId", user.getUserId().toString())
                .put("fullName", user.getFullName())
                .put("email", user.getEmail())
                .put("role", user.getRole().toString())
                .put("isActive", user.getIsActive());

        // 3. Append Specific Data based on Role
        if (user.getRole() == Role.STUDENT && user.getStudentProfile() != null) {
            response.put("profile", new JsonObject()
                            .put("profileId", user.getStudentProfile().getId())
                            .put("courseEnrolled", user.getStudentProfile().getCourseEnrolled())
                    // Add other student fields here
            );
        } else if (user.getRole() == Role.TEACHER && user.getTeacherProfile() != null) {
            response.put("profile", new JsonObject()
                            .put("profileId", user.getTeacherProfile().getId())
                            .put("qualification", user.getTeacherProfile().getQualification())
                            .put("experienceYears", user.getTeacherProfile().getExperienceYears())
                    // Add other teacher fields here
            );
        }
        ctx.response().putHeader("Content-Type", "application/json").end(response.encode());
    }
}