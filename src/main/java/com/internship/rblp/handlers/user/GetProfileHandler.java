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

    @Override
    public void handle(RoutingContext ctx) {
        if (this == GET_PROFILE) {
            handleGetProfile(ctx);
        }
    }

    private void handleGetProfile(RoutingContext ctx) {
        String userIdStr = ctx.get("userId");
        if (userIdStr == null) {
            ctx.fail(401);
            return;
        }

        UUID userId = UUID.fromString(userIdStr);
        User user = DB.find(User.class)
                .fetch("studentProfile")
                .fetch("teacherProfile")
                .setId(userId)
                .findOne();

        if (user == null) {
            ctx.response().setStatusCode(404).end(new JsonObject().put("error", "User not found").encode());
            return;
        }


        JsonObject response = new JsonObject()
                .put("userId", user.getUserId().toString())
                .put("fullName", user.getFullName())
                .put("email", user.getEmail())
                .put("role", user.getRole().toString())
                .put("isActive", user.getIsActive());


        if (user.getRole() == Role.STUDENT && user.getStudentProfile() != null) {
            response.put("profile", new JsonObject()
                            .put("profileId", user.getStudentProfile().getId())
                            .put("courseEnrolled", user.getStudentProfile().getCourseEnrolled())

            );
        } else if (user.getRole() == Role.TEACHER && user.getTeacherProfile() != null) {
            response.put("profile", new JsonObject()
                            .put("profileId", user.getTeacherProfile().getId())
                            .put("qualification", user.getTeacherProfile().getQualification())
                            .put("experienceYears", user.getTeacherProfile().getExperienceYears())

            );
        }
        ctx.response().putHeader("Content-Type", "application/json").end(response.encode());
    }
}