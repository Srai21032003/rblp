package com.internship.rblp.service;

import com.internship.rblp.models.entities.KycAiAnalysis;
import com.internship.rblp.models.entities.KycDetails;
import com.internship.rblp.models.entities.KycDocument;
import com.internship.rblp.models.enums.KycStatus;
import com.internship.rblp.repository.KycAiAnalysisRepository;
import com.internship.rblp.repository.KycRepository;
import io.ebean.DB;
import io.ebean.Transaction;
import io.github.cdimascio.dotenv.Dotenv;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiKycServiceGemini {

    private static final Logger logger = LoggerFactory.getLogger(AiKycServiceGemini.class);
    private final Vertx vertx;
    private final KycAiAnalysisRepository aiRepository;
    private final KycRepository kycRepository;
    private final WebClient webClient;

    private static final String MODEL_NAME = "gemini-3-flash-preview";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent";

    private static final String API_KEY = Dotenv.load().get("GEMINI_API_KEY");

    public AiKycServiceGemini(Vertx vertx, KycAiAnalysisRepository aiRepository, KycRepository kycRepository) {
        this.vertx = vertx;
        this.aiRepository = aiRepository;
        this.kycRepository = kycRepository;

        WebClientOptions options = new WebClientOptions()
                .setUserAgent("RBLP-KYC-System/1.0")
                .setKeepAlive(true)
                .setSsl(true);
        this.webClient = WebClient.create(vertx, options);
    }

    public void triggerAiReview(KycDetails kycDetails, List<KycDocument> documents) {
        Completable.fromAction(() -> {
            logger.info("Starting ai review for kyc: {}", kycDetails.getId());

            JsonObject payload = buildGeminiPayload(kycDetails, documents);
            String finalUrl = BASE_URL + "?key=" + API_KEY;

            webClient.postAbs(finalUrl)
                    .putHeader("Content-Type", "application/json")
                    .sendJsonObject(payload)
                    .subscribe(
                            response -> {
                                if (response.statusCode() == 200) {
                                    JsonObject body = response.bodyAsJsonObject();
                                    parseAndSaveGeminiResult(kycDetails, body);
                                } else {
                                    logger.error("Google AI API Failed: {} - {}", response.statusCode(), response.bodyAsString());
                                    saveFailure(kycDetails, "API Error: " + response.statusCode());
                                }
                            },
                            err -> {
                                logger.error("AI Network Error", err);
                                saveFailure(kycDetails, "Network Error: " + err.getMessage());
                            }
                    );
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    private JsonObject buildGeminiPayload(KycDetails kyc, List<KycDocument> docs) {

        String systemPrompt = "You are a KYC Risk Compliance AI. " +
                "The user '" + kyc.getUser().getFullName() + "' has uploaded " + docs.size() + " images. " +
                "They are expected to be: PAN Card, Aadhaar Card, and Passport. " +
                "\n\n" +
                "Analyze ALL images and return a SINGLE JSON object with this schema: \n" +
                "{ \n" +
                "  \"detectedDocuments\": [\"List\", \"Of\", \"Types\", \"Found\"], \n" +
                "  \"confidenceScores\": (Integer 0-100, overall confidence that these are valid ID docs matching the user FOR EACH DOC SEPARATELY as a list) eg. [40,80,90], \n" +
                "  \"overallConfidence\": (Integer 0-100, overall confidence that these are valid ID docs matching the user), \n" +
                "  \"extractedNames\": [\"Name from doc 1\", \"Name from doc 2\"], \n" +
                "  \"extractedDocNumbers\": [\"Document number from doc 1\", \"Document number from doc 2\", \"Document number from doc 1\"], \n" +
                "  \"extractedAddress\": \"Extracted address from any document\", \n" +
                "  \"extractedDob\": \"Extracted DOB from any document\", \n" +
                "  \"riskFlags\": [\"NAME_MISMATCH\", \"BLURRY_IMAGE\", \"MISSING_DOCUMENT\", \"LOOKS_FORGED\", \"EXPIRED\"], \n" +
                "  \"aiRecommendation\": \"CLEAR\" or \"MANUAL_REVIEW\" \n" +
                "  \"errorMessage\": (Reason for the risk flag), \n" +
                "} \n" +
                "Rules: \n" +
                "1. If names on documents do not loosely match '" + kyc.getUser().getFullName() + "', add 'NAME_MISMATCH'.\n" +
                "2. If an image is not a valid ID, add 'INVALID_DOCUMENT'.\n" +
                "3. Return ONLY raw JSON. No Markdown." +
                "4. Do not check for expiry date for Aadhaar and Pan Card." +
                "5. The risk flags should be in the same order the documents are read." +
                "6. Date of birth should match on all documents." +
                "7. The risk flag names should be preceded by the name of the document type to which they are associated, eg., for a NAME_MISMATCH with AADHAAR the risk flag should be: AADHAAR_NAME_MISMATCH. Same for PAN and PASSPORT." +
                "8. Provide confidence score of each document type individually as a list in the following format: [AADHAAR = 40, PAN = 80, PASSPORT = 0]" +
                "9. The format for extractedDob should strictly be as following: 2017-01-23";

        JsonArray parts = new JsonArray();

        parts.add(new JsonObject().put("text", systemPrompt));

        for (KycDocument doc : docs) {
            try {
                byte[] fileBytes = vertx.fileSystem().readFileBlocking(doc.getFilePath()).getBytes();
                String base64Img = java.util.Base64.getEncoder().encodeToString(fileBytes);

                String lowerPath = doc.getFilePath().toLowerCase();
                String mimeType = "image/jpeg";
                if (lowerPath.endsWith(".png")) mimeType = "image/png";
                else if (lowerPath.endsWith(".pdf")) mimeType = "application/pdf";

                parts.add(new JsonObject()
                        .put("inline_data", new JsonObject()
                                .put("mime_type", mimeType)
                                .put("data", base64Img)
                        )
                );
            } catch (Exception e) {
                logger.warn("Could not read file for AI: {}", doc.getFilePath());
            }
        }

        return new JsonObject()
                .put("contents", new JsonArray()
                        .add(new JsonObject()
                                .put("parts", parts)
                        )
                );
    }

    private void parseAndSaveGeminiResult(KycDetails kyc, JsonObject aiResponse) {
        try(Transaction txn = DB.beginTransaction()) {
            JsonArray candidates = aiResponse.getJsonArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("No candidates returned from AI");
            }

            String contentText = candidates.getJsonObject(0)
                    .getJsonObject("content")
                    .getJsonArray("parts")
                    .getJsonObject(0)
                    .getString("text");

            logger.info("Raw AI Response: {}", contentText); // Debugging

            String jsonString = contentText;
            Pattern pattern = Pattern.compile("\\{.*}", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(contentText);
            if (matcher.find()) {
                jsonString = matcher.group();
            }

            JsonObject resultJson = new JsonObject(jsonString);

            KycAiAnalysis analysis = new KycAiAnalysis();
            analysis.setKycDetails(kyc);
            analysis.setCreatedAt(Instant.now());

            JsonArray confidenceScores = resultJson.getJsonArray("confidenceScores");
            List<String> confidenceList = new ArrayList<>();
            if (confidenceScores != null) {
                confidenceScores.forEach(f -> confidenceList.add(f.toString()));
            }
            Integer overallConfidence = resultJson.getInteger("overallConfidence", 0);
            analysis.setOverallConfidence(overallConfidence);

//            analysis.setConfidenceScore(resultJson.getInteger("confidenceScore", 0));
            analysis.setRecommendation(resultJson.getString("aiRecommendation", "MANUAL_REVIEW"));

            analysis.setErrorMessage(resultJson.getString("errorMessage", ""));
            analysis.setRawResponse(resultJson.getMap());
            kyc.setAddress(resultJson.getString("extractedAddress", ""));
            kyc.setDob(LocalDate.parse(resultJson.getString("extractedDob", "")));

            if(overallConfidence < 50){
                kyc.setStatus(KycStatus.REJECTED);
                kyc.setAdminRemarks("Auto Rejected due to low confidence score: " + overallConfidence);
            }


            JsonArray flags = resultJson.getJsonArray("riskFlags");
            List<String> flagList = new ArrayList<>();
            if (flags != null) {
                flags.forEach(f -> flagList.add(f.toString()));
            }
            analysis.setRiskFlags(flagList);

            if ("CLEAR".equalsIgnoreCase(analysis.getRecommendation()) && analysis.getOverallConfidence() > 80) {
                analysis.setAiStatus("AI_CLEAR");
            } else {
                analysis.setAiStatus("AI_FLAGGED");
            }
            kyc.setStatus(KycStatus.PENDING);
            kycRepository.save(kyc);

            aiRepository.save(analysis);
            txn.commit();
            logger.info("AI Analysis Saved for KYC: {}", kyc.getId());

        } catch (Exception e) {
            logger.error("Failed to parse AI response: {}", aiResponse.encode(), e);
            saveFailure(kyc, "Parse Error: " + e.getMessage());
        }
    }

    private void saveFailure(KycDetails kyc, String reason) {
        KycAiAnalysis analysis = new KycAiAnalysis();
        analysis.setKycDetails(kyc);
        analysis.setAiStatus("AI_FAILED");
        analysis.setErrorMessage(reason);
        analysis.setCreatedAt(Instant.now());
        aiRepository.save(analysis);
    }
}