package com.groupeisi.sante.dossier.dto;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Instant occurredAt,
        String username,
        String action,
        String resourceType,
        Long resourceId,
        String clientIp
) {
}
