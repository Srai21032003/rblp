package com.internship.rblp.repository;

import com.internship.rblp.models.entities.User;
import io.ebean.DB;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepository {

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(
                DB.find(User.class).where().eq("email", email).findOne()
        );
    }

    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(DB.find(User.class, id));
    }

    public Optional<User> findByIdWithProfile(UUID id) {
        return Optional.ofNullable(
                DB.find(User.class)
                        .fetch("studentProfile")
                        .fetch("teacherProfile")
                        .setId(id)
                        .findOne()
        );
    }

    public boolean existsByEmail(String email) {
        return DB.find(User.class).where().eq("email", email).exists();
    }

    public List<User> findAll() {
        return DB.find(User.class).findList();
    }

    public void save(User user) {
        user.save();
    }
}