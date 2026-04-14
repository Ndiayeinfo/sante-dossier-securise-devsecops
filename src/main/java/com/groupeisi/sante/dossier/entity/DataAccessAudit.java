package com.groupeisi.sante.dossier.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "data_access_audit", indexes = {
        @Index(name = "idx_audit_resource", columnList = "resourceType,resourceId"),
        @Index(name = "idx_audit_time", columnList = "occurredAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataAccessAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false, length = 128)
    private String username;

    @Column(nullable = false, length = 32)
    private String action;

    @Column(nullable = false, length = 64)
    private String resourceType;

    private Long resourceId;

    @Column(length = 64)
    private String clientIp;

    @Column(length = 512)
    private String userAgent;
}
