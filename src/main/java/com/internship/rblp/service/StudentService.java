package com.internship.rblp.service;

import com.internship.rblp.models.entities.StudentProfile;
import com.internship.rblp.models.entities.User;
import io.ebean.DB;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

public class StudentService {

    public Single<JsonObject> updateProfile(String userIdStr, JsonObject data) {
        return Single.fromCallable(() -> {
            UUID userId = UUID.fromString(userIdStr);

            // Fetch User AND StudentProfile
            User user = DB.find(User.class)
                    .fetch("studentProfile")
                    .setId(userId)
                    .findOne();

            if (user == null) throw new RuntimeException("User not found");
            StudentProfile profile = user.getStudentProfile();

            if (profile == null) {
                profile = new StudentProfile();
                profile.setUser(user);
                user.setStudentProfile(profile);
            }

            // update fields
            if (data.containsKey("courseEnrolled")) profile.setCourseEnrolled(data.getString("courseEnrolled"));

            profile.save();

            return new JsonObject()
                    .put("courseEnrolled", profile.getCourseEnrolled());
        });
    }
}