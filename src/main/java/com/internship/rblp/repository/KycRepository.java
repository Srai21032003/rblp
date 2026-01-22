package com.internship.rblp.repository;

import com.internship.rblp.models.entities.KycDetails;
import com.internship.rblp.models.entities.KycDocument;
import io.ebean.DB;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class KycRepository {

    public Optional<KycDetails> findByUserId(UUID userId) {
        return Optional.ofNullable(
                DB.find(KycDetails.class).where().eq("user_id", userId).findOne()
        );
    }

    public Optional<KycDetails> findById(UUID id) {
        return Optional.ofNullable(DB.find(KycDetails.class, id));
    }

    public Optional<KycDetails> findByIdWithDocs(UUID id) {
        return Optional.ofNullable(
                DB.find(KycDetails.class)
                        .fetch("documents")
                        .fetch("user", "fullName, email")
                        .setId(id)
                        .findOne()
        );
    }

    public Optional<KycDetails> findByUserIdWithDocs(UUID userId) {
        return Optional.ofNullable(
                DB.find(KycDetails.class)
                        .fetch("documents")
                        .where().eq("user_id", userId).findOne()
        );
    }

    public List<KycDetails> findAllWithUser() {
        return DB.find(KycDetails.class)
                .fetch("user", "email, fullName, role")
                .findList();
    }

    public Optional<KycDocument> findDocumentById(UUID docId) {
        return Optional.ofNullable(DB.find(KycDocument.class, docId));
    }

    public void deleteDocumentsByKycId(KycDetails kycDetails) {
        DB.find(KycDocument.class).where().eq("kycDetails", kycDetails).delete();
    }

    public void save(KycDetails kycDetails) {
        kycDetails.save();
    }

    public void saveDocument(KycDocument document) {
        document.save();
    }
}