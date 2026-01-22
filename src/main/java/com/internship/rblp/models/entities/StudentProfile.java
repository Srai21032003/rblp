package com.internship.rblp.models.entities;

import io.ebean.Model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "student_profiles")
@ToString(exclude = "user")
public class StudentProfile extends Model {

    @Id
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String courseEnrolled;
}
