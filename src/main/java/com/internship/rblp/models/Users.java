package com.internship.rblp.models;

import io.ebean.Model;
import io.ebean.config.JsonConfig;
import jakarta.persistence.*;

@Entity
public class Users extends Model{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private JsonConfig.DateTime createdAt;

    @Column(nullable = false)
    private JsonConfig.DateTime updatedAt;

    private String fullName;

    @Id
    private String email;

    private String mobileNumber;

    private String password;

    private String status;



}
