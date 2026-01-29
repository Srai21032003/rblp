package com.internship.rblp.service;

import com.internship.rblp.models.entities.AuditLogs;
import com.internship.rblp.models.entities.User;
import com.internship.rblp.models.enums.AuditAction;
import com.internship.rblp.repository.AuditLogsRepository;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

public class AuditLogsService {
    private final AuditLogsRepository auditRepo;
    private static final Logger logger = LoggerFactory.getLogger(AuditLogsService.class);

    public AuditLogsService(AuditLogsRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    public Completable logAction(RoutingContext ctx, AuditAction action,
                                        String status, String details) {
        return Completable.fromAction(() -> {
            try{
                String userId = ctx.get("userId");
                if(userId == null){
                    logger.warn("Audit log was skipped: userId not found in ctx");
                    return;
                }

                UUID userUuid = UUID.fromString(userId);
                AuditLogs auditLogs = auditRepo.findByUserId(userUuid)
                        .orElseGet(() -> createNewAuditLog(userUuid));
                String timestamp = getCurrentTimestamp();
                String ipAddress = extractIpAddress(ctx);
                String userAgent = extractUserAgent(ctx);

                AuditLogs.AuditLogEntry entry = new AuditLogs.AuditLogEntry(
                        timestamp,
                        action.getDescription(),
                        userAgent,
                        ipAddress,
                        details,
                        status
                );
                auditLogs.getLogs().add(entry);
                auditRepo.save(auditLogs);
            }catch (Exception e){
                logger.error("Critical: Audit log failed for action: {}",action, e);
            }
        }).subscribeOn(Schedulers.io())
                .onErrorComplete();

    }

    public Completable logSuccess(RoutingContext ctx, AuditAction action) {
        return logAction(ctx, action, "SUCCESS", null);
    }

    public Completable logSuccess(RoutingContext ctx, AuditAction action, String details) {
        return logAction(ctx, action, "SUCCESS", details);
    }

    public Completable logFailure(RoutingContext ctx, AuditAction action, String error) {
        return logAction(ctx, action, "FAILURE", error);
    }

    private String extractUserAgent(RoutingContext ctx) {
        return ctx.request().getHeader("User-Agent");
    }

    private String extractIpAddress(RoutingContext ctx) {
        String extracted = ctx.request().getHeader("X-Forwarded-For");
        if(extracted != null && !extracted.isEmpty()){
            return extracted.split(",")[0].trim();
        }
        return ctx.request().remoteAddress().host();
    }

    private AuditLogs createNewAuditLog(UUID userUuid) {
        AuditLogs newLog = new AuditLogs();
        User user = new User();
        user.setUserId(userUuid);
        newLog.setUser(user);
        newLog.setLogs(new ArrayList<>());
        return newLog;
    }

    public String getCurrentTimestamp() {
        return Instant.now()
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy"));
    }
}
