package com.internship.rblp.service;

import com.internship.rblp.models.entities.AuditLogs;
import com.internship.rblp.models.entities.User;
import com.internship.rblp.repository.AuditLogsRepository;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

public class AuditLogsService {
    private final AuditLogsRepository auditRepo;

    public AuditLogsService(AuditLogsRepository auditRepo) {
        this.auditRepo = auditRepo;
    }
    public Completable addAuditLogEntry(RoutingContext ctx, String actionString) {
        return Completable.fromAction(() -> {
            String userId = ctx.get("userId");
            UUID userUuid = UUID.fromString(userId);
            AuditLogs auditLogs = auditRepo.findById(userUuid)
                    .orElseGet(() ->{
                        AuditLogs newLog = new AuditLogs();
                        User user = new User();
                        user.setUserId(userUuid);
                        newLog.setUser(user);
                        newLog.setLogs(new ArrayList<>());
                        return newLog;
                    });
            auditLogs.getLogs().add(new AuditLogs.AuditLogEntry(Instant.now(), actionString));
            auditRepo.save(auditLogs);
        }).subscribeOn(Schedulers.io());
    }
}
