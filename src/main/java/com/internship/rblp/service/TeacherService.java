package com.internship.rblp.service;

import com.internship.rblp.models.entities.TeacherProfile;
import com.internship.rblp.models.entities.User;
import com.internship.rblp.repository.UserRepository;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

public class TeacherService {

    private final UserRepository userRepository;

    public TeacherService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Single<JsonObject> updateProfile(String userIdStr, JsonObject data) {
        return Single.fromCallable(() -> {
            UUID userId = UUID.fromString(userIdStr);

            User user = userRepository.findByIdWithProfile(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            TeacherProfile profile = user.getTeacherProfile();

            if (profile == null) {
                profile = new TeacherProfile();
                profile.setUser(user);
                user.setTeacherProfile(profile);
            }

            if (data.containsKey("experienceYears")) profile.setExperienceYears(data.getInteger("experienceYears"));
            if (data.containsKey("qualification")) profile.setQualification(data.getString("qualification"));

            userRepository.save(user);

            return new JsonObject()
                    .put("experienceYears", profile.getExperienceYears())
                    .put("qualification", profile.getQualification());
        }).subscribeOn(Schedulers.io());
    }
}