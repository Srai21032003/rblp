package com.internship.rblp.service;

import com.internship.rblp.models.entities.StudentProfile;
import com.internship.rblp.models.entities.User;
import com.internship.rblp.repository.UserRepository;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

public class StudentService {

    private final UserRepository userRepository;

    public StudentService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Single<JsonObject> updateProfile(String userIdStr, JsonObject data) {
        return Single.fromCallable(() -> {
            UUID userId = UUID.fromString(userIdStr);

            // Use Repo method that fetches profile eagerly
            User user = userRepository.findByIdWithProfile(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            StudentProfile profile = user.getStudentProfile();

            // Safety check: Create profile if missing (though registration should handle this)
            if (profile == null) {
                profile = new StudentProfile();
                profile.setUser(user);
                user.setStudentProfile(profile);
            }

            // Update allowed fields
            if (data.containsKey("courseEnrolled")) profile.setCourseEnrolled(data.getString("courseEnrolled"));

            // Saving the User aggregate root cascades to profile (assuming CascadeType.ALL)
            userRepository.save(user);

            return new JsonObject()
                    .put("courseEnrolled", profile.getCourseEnrolled());
        });
    }
}