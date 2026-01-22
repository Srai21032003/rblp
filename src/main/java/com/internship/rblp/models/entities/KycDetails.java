package com.internship.rblp.models.entities;

import com.internship.rblp.models.enums.KycStatus;
import io.ebean.Model;
import io.ebean.annotation.NotNull;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "kyc_details")
@ToString(exclude = {"user", "documents"}) // Prevent circular loop
public class KycDetails extends Model {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    private String address;
    private LocalDate dob;

    @NotNull
    @Enumerated(EnumType.STRING)
    private KycStatus status = KycStatus.PENDING;

    private String adminRemarks;

    @OneToMany(mappedBy = "kycDetails", cascade = CascadeType.ALL)
    private List<KycDocument> documents;

    @WhenCreated
    private Instant createdAt;

    @WhenModified
    private Instant updatedAt;
}
