package com.internship.rblp.service;

import com.internship.rblp.models.entities.StudentProfile;
import com.internship.rblp.models.entities.TeacherProfile;
import com.internship.rblp.models.entities.User;
import com.internship.rblp.models.enums.Role;
import com.internship.rblp.repository.UserRepository; // Import Repo
import com.internship.rblp.util.JwtUtil;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Single<String> login(String email, String password) {
        return Single.fromCallable(() -> {
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }
            if (!user.getIsActive()) {
                throw new RuntimeException("Account is disabled. Contact Admin.");
            }

            return JwtUtil.generateToken(user.getUserId(), user.getRole(), user.getEmail());
        });
    }

    public Single<String> register(JsonObject data) {
        return Single.fromCallable(() -> {
            String email = data.getString("email");

            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email already registered");
            }

            User user = new User();
            user.setFullName(data.getString("fullName"));
            user.setEmail(email);
            user.setPassword(BCrypt.hashpw(data.getString("password"), BCrypt.gensalt()));
            user.setRole(Role.valueOf(data.getString("role").toUpperCase()));
            user.setIsActive(true);

            if (user.getRole() == Role.STUDENT) {
                user.setStudentProfile(new StudentProfile());
                user.getStudentProfile().setUser(user);
            } else if (user.getRole() == Role.TEACHER) {
                user.setTeacherProfile(new TeacherProfile());
                user.getTeacherProfile().setUser(user);
            }

            userRepository.save(user);
            return JwtUtil.generateToken(user.getUserId(), user.getRole(), user.getEmail());
        });
    }
}