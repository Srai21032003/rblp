package com.internship.rblp.service;

import com.internship.rblp.models.entities.StudentProfile;
import com.internship.rblp.models.entities.User;
import com.internship.rblp.repository.UserRepository;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class StudentService {

    private final UserRepository userRepository;

    public StudentService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Single<JsonObject> updateProfile(String userIdStr, JsonObject data) {
        return Single.fromCallable(() -> {
            UUID userId = UUID.fromString(userIdStr);

            User user = userRepository.findByIdWithProfile(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            User user1 = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (data.containsKey("email")) {
                user.setEmail(data.getString("email"));
            }

            StudentProfile profile = user.getStudentProfile();

            if (profile == null) {
                profile = new StudentProfile();
                profile.setUser(user);
                user.setStudentProfile(profile);
            }

//            Optional<User> userProfile = userRepository.findById(userId);
//
//            if(userProfile.isEmpty()){
//                throw new RuntimeException("User not found");
//            }

//            User userP = userProfile.get();


            if (data.containsKey("courseEnrolled")) profile.setCourseEnrolled(data.getString("courseEnrolled"));
//            if(data.containsKey("email")) userP.setEmail(data.getString("email"));

            user1.setUpdatedAt(Instant.now());
            userRepository.save(user);

            return new JsonObject()
                    .put("courseEnrolled", profile.getCourseEnrolled())
                    .put("email", user.getEmail())
                    .put("updatedAt", user1.getUpdatedAt());
        });
    }
}