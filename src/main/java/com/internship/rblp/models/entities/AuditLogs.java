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
@Table(name = "audit_logs")
public class AuditLogs extends Model {

    @Id
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @DbJson
    @Column(name = "audit_logs")
    private List<AuditLogEntry> logs;

    @Getter
    @Setter
    public static class AuditLogEntry {
        private String timestamp;
        private String action;

        public AuditLogEntry(){}

        public AuditLogEntry(Instant now, String actionString) {
            this.timestamp = now.toString();
            this.action = actionString;
        }
    }
}
