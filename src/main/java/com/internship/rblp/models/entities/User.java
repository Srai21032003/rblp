package com.internship.rblp.models.entities;

import com.internship.rblp.models.enums.Role;
import io.ebean.Model;
import io.ebean.annotation.NotNull;
import io.ebean.annotation.SoftDelete;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
@ToString(exclude = {"teacherProfile","studentProfile","kycDetails"})
public class User extends Model{
    @Id
    @GeneratedValue
    private UUID userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull
    private String fullName;

    @NotNull
    @Column(unique = true)
    private String email;

    private String mobileNumber;

    @NotNull
    private String password;

    @NotNull
    private Boolean isActive = true;

    //reference to teacher and student profile
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private TeacherProfile teacherProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private StudentProfile studentProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private KycDetails kycDetails;

    @SoftDelete
    private boolean deleted;

    @WhenCreated
    private Instant createdAt;

    @WhenModified
    private Instant updatedAt;

}
