package com.internship.rblp.models.entities;

import com.internship.rblp.models.enums.DocType;
import com.internship.rblp.models.enums.ValidationStatus;
import io.ebean.Model;
import io.ebean.annotation.NotNull;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "kyc_documents")
@ToString(exclude = "kycDetails")
public class KycDocument extends Model {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "kyc_details_id", nullable = false)
    private KycDetails kycDetails;

    @NotNull
    @Enumerated(EnumType.STRING)
    private DocType docType;

    private String documentNumber;

    @NotNull
    private String filePath;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ValidationStatus validationStatus = ValidationStatus.MANUAL_REVIEW;

    private String validationMessage;
}
