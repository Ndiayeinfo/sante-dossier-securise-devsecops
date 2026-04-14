package com.groupeisi.sante.dossier.web;

import com.groupeisi.sante.dossier.dto.AuditLogResponse;
import com.groupeisi.sante.dossier.entity.DataAccessAudit;
import com.groupeisi.sante.dossier.audit.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
@Tag(name = "Audit sécurité", description = "Traçabilité des accès (rôle administrateur sécurité)")
public class SecurityAuditController {

    private final AuditService auditService;

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN_SECURITE')")
    @Operation(summary = "Derniers événements d'accès aux données")
    public List<AuditLogResponse> recentAudit() {
        return auditService.recent().stream().map(this::map).toList();
    }

    private AuditLogResponse map(DataAccessAudit a) {
        return new AuditLogResponse(
                a.getId(),
                a.getOccurredAt(),
                a.getUsername(),
                a.getAction(),
                a.getResourceType(),
                a.getResourceId(),
                a.getClientIp()
        );
    }
}
