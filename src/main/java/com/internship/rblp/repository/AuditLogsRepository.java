package com.internship.rblp.repository;

import com.internship.rblp.models.entities.AuditLogs;
import com.internship.rblp.models.entities.User;
import io.ebean.DB;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.Optional;
import java.util.UUID;

public class AuditLogsRepository {
    public Optional<AuditLogs> findById(UUID id) {
        return Optional.ofNullable(DB.find(AuditLogs.class, id));
    }

    public void save(AuditLogs auditLogs) {
        auditLogs.save();
    }

    public Optional<AuditLogs> findByUserId(UUID userId) {
        return DB.find(AuditLogs.class)
                .where()
                .eq("user.userId", userId)
                .findOneOrEmpty();
    }


}
