package com.internship.rblp.repository;

import com.internship.rblp.models.entities.KycAiAnalysis;
import io.ebean.DB;

import java.util.Optional;
import java.util.UUID;

public class KycAiAnalysisRepository {

    public void save(KycAiAnalysis analysis) {
        DB.save(analysis);
    }

    public Optional<KycAiAnalysis> findByKycId(UUID kycId) {
        return DB.find(KycAiAnalysis.class)
                .where().eq("kycDetails.id", kycId)
                .findOneOrEmpty();
    }
}