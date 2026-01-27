package com.internship.rblp.service;

import com.internship.rblp.models.entities.KycAiAnalysis;
import com.internship.rblp.models.entities.KycDetails;
import com.internship.rblp.models.entities.KycDocument;
import com.internship.rblp.repository.KycAiAnalysisRepository;
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
import java.util.ArrayList;
import java.util.List;

public class AiKycServiceLlama {

    private static final Logger logger = LoggerFactory.getLogger(AiKycServiceLlama.class);
    private final Vertx vertx;
    private final KycAiAnalysisRepository aiRepository;
    private final WebClient webClient;

    private static final String MODEL_NAME = "meta-llama/llama-3.3-70b-instruct:free";
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    private static final String API_KEY = "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";

    public AiKycServiceLlama(Vertx vertx, KycAiAnalysisRepository aiRepository) {
        this.vertx = vertx;
        this.aiRepository = aiRepository;

        WebClientOptions options = new WebClientOptions()
                .setUserAgent("RBLP-KYC-System/1.0")
                .setKeepAlive(true)
                .setSsl(true);
        this.webClient = WebClient.create(vertx, options);
    }

    public void triggerAiReview(KycDetails kycDetails, List<KycDocument> documents) {
        Completable.fromAction(() -> {
            logger.info("Starting AI Review (Llama 3) for KYC: {}", kycDetails.getId());

            JsonObject payload = buildOpenRouterPayload(kycDetails, documents);

            webClient.postAbs(API_URL)
                    .putHeader("Authorization", "Bearer " + API_KEY)
                    .putHeader("Content-Type", "application/json")
                    // OpenRouter requires these headers for rankings
                    .putHeader("HTTP-Referer", "http://localhost:8080")
                    .putHeader("X-Title", "RBLP Internship Project")
                    .sendJsonObject(payload)
                    .subscribe(
                            response -> {
                                if (response.statusCode() == 200) {
                                    JsonObject body = response.bodyAsJsonObject();
                                    parseAndSaveOpenAiResult(kycDetails, body);
                                } else {
                                    logger.error("AI API Failed: {} - {}", response.statusCode(), response.bodyAsString());
                                    saveFailure(kycDetails, "API Error: " + response.statusMessage());
                                }
                            },
                            err -> {
                                logger.error("AI Network Error", err);
                                saveFailure(kycDetails, "Network Error: " + err.getMessage());
                            }
                    );
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    private JsonObject buildOpenRouterPayload(KycDetails kyc, List<KycDocument> docs) {
        String systemPrompt = "You are a KYC Risk Compliance AI. Analyze the provided document images. " +
                "Return ONLY a raw JSON object (no markdown) with this schema: " +
                "{ " +
                "  \"documentTypeDetected\": \"PAN/AADHAAR/PASSPORT/UNKNOWN\", " +
                "  \"confidenceScore\": (0-100 integer), " +
                "  \"extractedName\": \"...\", " +
                "  \"extractedDocumentNumber\": \"...\", " +
                "  \"riskFlags\": [\"BLURRY_IMAGE\", \"NAME_MISMATCH\", \"WRONG_DOCUMENT_TYPE\", \"INVALID_FORMAT\", \"LOOKS_FORGED\"], " +
                "  \"aiRecommendation\": \"CLEAR\" or \"MANUAL_REVIEW\" " +
                "} " +
                "Compare extracted name with User Profile Name: '" + kyc.getUser().getFullName() + "'.";

        JsonArray contentParts = new JsonArray();

        contentParts.add(new JsonObject()
                .put("type", "text")
                .put("text", systemPrompt)
        );

        for (KycDocument doc : docs) {
            try {
                byte[] fileBytes = vertx.fileSystem().readFileBlocking(doc.getFilePath()).getBytes();
                String base64Img = java.util.Base64.getEncoder().encodeToString(fileBytes);

                String mimeType = doc.getFilePath().endsWith(".png") ? "image/png" : "image/jpeg";

                contentParts.add(new JsonObject()
                        .put("type", "image_url")
                        .put("image_url", new JsonObject()
                                .put("url", "data:" + mimeType + ";base64," + base64Img)
                        )
                );
            } catch (Exception e) {
                logger.warn("Could not read file for AI: {}", doc.getFilePath());
            }
        }

        return new JsonObject()
                .put("model", MODEL_NAME)
                .put("messages", new JsonArray()
                        .add(new JsonObject()
                                .put("role", "user")
                                .put("content", contentParts)
                        )
                );
    }

    private void parseAndSaveOpenAiResult(KycDetails kyc, JsonObject aiResponse) {
        try {
            JsonArray choices = aiResponse.getJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("No choices returned from AI");
            }

            String contentText = choices.getJsonObject(0)
                    .getJsonObject("message")
                    .getString("content");

            if (contentText.contains("```json")) {
                contentText = contentText.substring(contentText.indexOf("```json") + 7);
                if (contentText.contains("```")) {
                    contentText = contentText.substring(0, contentText.lastIndexOf("```"));
                }
            } else if (contentText.contains("```")) {
                contentText = contentText.replace("```", "");
            }

            JsonObject resultJson = new JsonObject(contentText.trim());

            KycAiAnalysis analysis = new KycAiAnalysis();
            analysis.setKycDetails(kyc);
            analysis.setCreatedAt(Instant.now());

            analysis.setOverallConfidence(resultJson.getInteger("confidenceScore", 0));
            analysis.setRecommendation(resultJson.getString("aiRecommendation", "MANUAL_REVIEW"));
            analysis.setRawResponse(resultJson.getMap());

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

            aiRepository.save(analysis);
            logger.info("AI Analysis Saved for KYC: {}", kyc.getId());

        } catch (Exception e) {
            logger.error("Failed to parse AI response: " + aiResponse.encodePrettily(), e);
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