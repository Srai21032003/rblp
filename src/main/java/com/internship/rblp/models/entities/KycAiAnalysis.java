package com.internship.rblp.models.entities;

import io.ebean.Model;
import io.ebean.annotation.DbJson;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Getter
@Setter
@Entity
@Table(name = "kyc_ai_analysis")
public class KycAiAnalysis extends Model {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "kyc_details_id", nullable = false)
    private KycDetails kycDetails;

    @Column(name = "ai_status")
    private String aiStatus;

    @DbJson
    @Column(name = "confidence_scores")
    private List<String> confidenceScores;

    @Column(name = "overallConfidence")
    private Integer overallConfidence;

    @Column(name = "recommendation")
    private String recommendation;

    @DbJson
    @Column(name = "risk_flags")
    private List<String> riskFlags;

    @DbJson
    @Column(name = "raw_response")
    private Map<String, Object> rawResponse;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
