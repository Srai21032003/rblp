package com.internship.rblp.service;

import com.internship.rblp.models.entities.StudentProfile;
import com.internship.rblp.models.entities.TeacherProfile;
import com.internship.rblp.models.entities.User;
import com.internship.rblp.models.enums.Role;
import com.internship.rblp.routers.UserRouter;
import io.ebean.DB;
import io.ebean.Transaction;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.UUID;

public class AdminService {

    public Single<JsonObject> onboardUser(JsonObject data){
        return Single.fromCallable(() -> {
           String email = data.getString("email");

           if(DB.find(User.class).where().eq("email",email).exists()){
               throw new RuntimeException("User already exists");
           }

           try(Transaction txn = DB.beginTransaction()){
               User user = new User();
               user.setFullName(data.getString("fullName"));
               user.setEmail(email);

               String rawPass = data.getString("password","Welcome123@");
               user.setPassword(BCrypt.hashpw(rawPass,BCrypt.gensalt()));
               user.setRole(Role.valueOf(data.getString("role").toUpperCase()));
               user.setIsActive(true);

               if (user.getRole() == Role.STUDENT) {
                   StudentProfile sp = new StudentProfile();
                   sp.setUser(user);
                   user.setStudentProfile(sp);
               } else if (user.getRole() == Role.TEACHER) {
                   TeacherProfile tp = new TeacherProfile();
                   tp.setUser(user);
                   user.setTeacherProfile(tp);
               }
               user.save();
               txn.commit();
               return new JsonObject().put("userId", user.getUserId().toString()).put("email",user.getEmail());
           }
        });
    }

    public Single<List<User>> getAllUsers(){
        return Single.fromCallable(() -> DB.find(User.class).findList());
    }

    public Single<JsonObject> toggleUserStatus(String userIdStr){
        return Single.fromCallable(() -> {
            UUID userId = UUID.fromString(userIdStr);
            User user = DB.find(User.class, userId);

            if(user == null) throw new RuntimeException("User not found");

            user.setIsActive(!user.getIsActive());
            user.save();

            return new JsonObject()
                    .put("userId", userIdStr)
                    .put("isActive", user.getIsActive())
                    .put("message", "User Status updated");
        });
    }
}
