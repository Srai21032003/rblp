package com.internship.rblp.service;

import com.internship.rblp.models.entities.TeacherProfile;
import com.internship.rblp.models.entities.User;
import io.ebean.DB;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

public class TeacherService {

    public Single<JsonObject> updateProfile(String userIdStr, JsonObject data) {
        return Single.fromCallable(() -> {
            UUID userId = UUID.fromString(userIdStr);

            User user = DB.find(User.class)
                    .fetch("teacherProfile")
                    .setId(userId)
                    .findOne();

            if (user == null) throw new RuntimeException("User not found");
            TeacherProfile profile = user.getTeacherProfile();

            if (profile == null) {
                profile = new TeacherProfile();
                profile.setUser(user);
                user.setTeacherProfile(profile);
            }

            // Update fields
            if (data.containsKey("experienceYears")) profile.setExperienceYears(data.getInteger("experienceYears"));
            if (data.containsKey("qualification")) profile.setQualification(data.getString("qualification"));


            profile.save();

            return new JsonObject()
                    .put("experienceYears", profile.getExperienceYears())
                    .put("qualification", profile.getQualification());
        });
    }
}