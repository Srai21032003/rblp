package com.internship.rblp.models.entities;

import io.ebean.Model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "bulk_upload_errors")
public class BulkUploadError extends Model {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "bulk_upload_id", nullable = false)
    private BulkUpload bulkUpload;

    // The email identifier from the CSV row that failed
    @Column(nullable = false)
    private String email;

    // Reason for failure (e.g., "Invalid Email", "Duplicate")
    @Column(nullable = false)
    private String reason;
}