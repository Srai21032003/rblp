package com.internship.rblp.service;

import com.internship.rblp.models.entities.KycDetails;
import com.internship.rblp.models.entities.KycDocument;
import com.internship.rblp.models.entities.User;
import com.internship.rblp.models.enums.DocType;
import com.internship.rblp.models.enums.KycStatus;
import com.internship.rblp.models.enums.ValidationStatus;
import com.internship.rblp.repository.KycRepository;
import com.internship.rblp.repository.UserRepository;
import io.ebean.DB;
import io.ebean.Transaction;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.LocalDate;
import java.util.*;

public class KycService {

    private final KycRepository kycRepository;
    private final UserRepository userRepository;

    private static final Set<String> REQUIRED_DOCS = Set.of("PAN", "AADHAAR", "PASSPORT");

    public KycService(KycRepository kycRepository, UserRepository userRepository) {
        this.kycRepository = kycRepository;
        this.userRepository = userRepository;
    }

    /**
     * SAMPLE PAYLOAD EXPECTED
     * {
     *     "address": "123 Main St, Pune",
     *     "dob": "1999-01-01",
     *     "documents": [
     *         {
     *             "docType": "PAN",
     *             "filePath": "/uploads/pan.jpg",
     *             "documentNumber": "ABCDE1234F"
     *         },
     *         {
     *             "docType": "AADHAAR",
     *             "filePath": "/uploads/aadhaar.jpg",
     *             "documentNumber": "123456789012"
     *         },
     *         {
     *             "docType": "PASSPORT",
     *             "filePath": "/uploads/passport.jpg",
     *             "documentNumber": "Z1234567"
     *         }
     *     ]
     * }
     */
    public Single<String> submitKyc(String userIdStr, JsonObject data) {
        return Single.fromCallable(() -> {
            UUID userId = UUID.fromString(userIdStr);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            JsonArray docsArray = data.getJsonArray("documents");
            if (docsArray == null || docsArray.size() < 3) {
                throw new RuntimeException("All 3 documents (PAN, AADHAAR, PASSPORT) are required.");
            }

            Set<String> uploadedTypes = new HashSet<>();
            for (int i = 0; i < docsArray.size(); i++) {
                try {
                    uploadedTypes.add(docsArray.getJsonObject(i).getString("docType"));
                } catch (Exception e) {
                    throw new RuntimeException("Invalid document structure in array");
                }
            }

            if (!uploadedTypes.containsAll(REQUIRED_DOCS)) {
                throw new RuntimeException("Missing required documents. You must upload PAN, AADHAAR, and PASSPORT.");
            }

            try (Transaction txn = DB.beginTransaction()) {

                KycDetails kycDetails = kycRepository.findByUserId(userId).orElse(null);

                if (kycDetails != null &&
                        (kycDetails.getStatus() == KycStatus.SUBMITTED || kycDetails.getStatus() == KycStatus.APPROVED)) {
                    throw new RuntimeException("KYC is already submitted or approved.");
                }

                if (kycDetails == null) {
                    kycDetails = new KycDetails();
                    kycDetails.setUser(user);
                    kycDetails.setCreatedAt(java.time.Instant.now());
                }

                if (data.containsKey("address")) kycDetails.setAddress(data.getString("address"));
                if (data.containsKey("dob")) kycDetails.setDob(LocalDate.parse(data.getString("dob")));

                kycDetails.setStatus(KycStatus.SUBMITTED);
                kycDetails.setUpdatedAt(java.time.Instant.now());

                kycRepository.save(kycDetails);

                kycRepository.deleteDocumentsByKycId(kycDetails);

                for (int i = 0; i < docsArray.size(); i++) {
                    JsonObject docJson = docsArray.getJsonObject(i);
                    KycDocument doc = new KycDocument();

                    doc.setKycDetails(kycDetails);
                    doc.setDocType(DocType.valueOf(docJson.getString("docType")));
                    doc.setFilePath(docJson.getString("filePath"));
                    doc.setDocumentNumber(docJson.getString("documentNumber"));

                    doc.setValidationStatus(ValidationStatus.MANUAL_REVIEW);

                    kycRepository.saveDocument(doc);
                }

                txn.commit();
                return kycDetails.getId().toString();
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable validateDocument(String docIdStr, String statusStr, String msg) {
        return Completable.fromAction(() -> {
            UUID docId = UUID.fromString(docIdStr);
            KycDocument doc = kycRepository.findDocumentById(docId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            doc.setValidationStatus(ValidationStatus.valueOf(statusStr));
            doc.setValidationMessage(msg);

            kycRepository.saveDocument(doc);
        }).subscribeOn(Schedulers.io());
    }

    public Single<JsonObject> getKycStatus(String userIdStr) {
        return Single.fromCallable(() -> {
            UUID userId = UUID.fromString(userIdStr);
            KycDetails kyc = kycRepository.findByUserIdWithDocs(userId).orElse(null);

            if (kyc == null) {
                return new JsonObject().put("status", KycStatus.NOT_SUBMITTED);
            }

            JsonArray docStatuses = new JsonArray();
            if (kyc.getDocuments() != null) {
                for (KycDocument doc : kyc.getDocuments()) {
                    docStatuses.add(new JsonObject()
                            .put("docType", doc.getDocType())
                            .put("validationStatus", doc.getValidationStatus())
                            .put("remarks", doc.getValidationMessage())
                    );
                }
            }

            return new JsonObject()
                    .put("status", kyc.getStatus())
                    .put("adminRemarks", kyc.getAdminRemarks())
                    .put("submissionDate", kyc.getCreatedAt().toString())
                    .put("documents", docStatuses);
        }).subscribeOn(Schedulers.io());
    }

    public Single<JsonArray> getAllSubmissions() {
        return Single.fromCallable(() -> {
            List<KycDetails> list = kycRepository.findAllWithUser();

            JsonArray result = new JsonArray();
            for (KycDetails k : list) {
                result.add(new JsonObject()
                        .put("kycId", k.getId())
                        .put("user", k.getUser().getFullName())
                        .put("email", k.getUser().getEmail())
                        .put("role", k.getUser().getRole())
                        .put("status", k.getStatus())
                        .put("date", k.getCreatedAt().toString())
                );
            }
            return result;
        }).subscribeOn(Schedulers.io());
    }

    public Single<JsonObject> getKycDetail(String kycIdStr) {
        return Single.fromCallable(() -> {
            UUID kycId = UUID.fromString(kycIdStr);
            KycDetails kyc = kycRepository.findByIdWithDocs(kycId)
                    .orElseThrow(() -> new RuntimeException("KYC not found"));

            JsonArray docsArray = new JsonArray();
            if (kyc.getDocuments() != null) {
                for (KycDocument doc : kyc.getDocuments()) {
                    docsArray.add(new JsonObject()
                            .put("docId", doc.getId())
                            .put("docType", doc.getDocType())
                            .put("docNumber", doc.getDocumentNumber())
                            .put("filePath", doc.getFilePath())
                            .put("validationStatus", doc.getValidationStatus())
                            .put("validationMsg", doc.getValidationMessage())
                    );
                }
            }

            return new JsonObject()
                    .put("kycId", kyc.getId().toString())
                    .put("user", kyc.getUser().getFullName())
                    .put("email", kyc.getUser().getEmail())
                    .put("address", kyc.getAddress())
                    .put("dob", kyc.getDob() != null ? kyc.getDob().toString() : "")
                    .put("status", kyc.getStatus())
                    .put("documents", docsArray);
        }).subscribeOn(Schedulers.io());
    }

    public Completable approveKyc(String kycIdStr) {
        return Completable.fromAction(() -> {
            UUID kycId = UUID.fromString(kycIdStr);
            KycDetails kyc = kycRepository.findById(kycId)
                    .orElseThrow(() -> new RuntimeException("KYC not found"));

            kyc.setStatus(KycStatus.APPROVED);
            kyc.setUpdatedAt(java.time.Instant.now());
            kycRepository.save(kyc);
        }).subscribeOn(Schedulers.io());
    }

    public Completable rejectKyc(String kycIdStr, String reason) {
        return Completable.fromAction(() -> {
            UUID kycId = UUID.fromString(kycIdStr);
            KycDetails kyc = kycRepository.findById(kycId)
                    .orElseThrow(() -> new RuntimeException("KYC not found"));

            kyc.setStatus(KycStatus.REJECTED);
            kyc.setAdminRemarks(reason);
            kyc.setUpdatedAt(java.time.Instant.now());
            kycRepository.save(kyc);
        }).subscribeOn(Schedulers.io());
    }
}