package com.internship.rblp.models.entities;

import com.internship.rblp.models.enums.KycStatus;
import com.internship.rblp.models.enums.Role;
import io.ebean.Model;
import io.ebean.annotation.NotNull;
import io.ebean.config.JsonConfig;
import jakarta.persistence.*;

@Entity
public class Users extends Model{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull
    private JsonConfig.DateTime createdAt;

    @Column(nullable = false)
    private JsonConfig.DateTime updatedAt;

    @NotNull
    private String fullName;

    @NotNull
    @Column(unique = true)
    private String email;

    private String mobileNumber;

    @NotNull
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    private KycStatus status;

    @NotNull
    private Boolean isActive = true;




}
