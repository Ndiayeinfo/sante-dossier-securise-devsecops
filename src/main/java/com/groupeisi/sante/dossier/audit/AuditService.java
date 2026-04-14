package com.groupeisi.sante.dossier.audit;

import com.groupeisi.sante.dossier.entity.DataAccessAudit;
import com.groupeisi.sante.dossier.repository.DataAccessAuditRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final DataAccessAuditRepository auditRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String resourceType, Long resourceId) {
        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "anonymous";
        String ip = null;
        String ua = null;
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            ip = req.getRemoteAddr();
            ua = req.getHeader("User-Agent");
            if (ua != null && ua.length() > 512) {
                ua = ua.substring(0, 512);
            }
        }
        auditRepository.save(DataAccessAudit.builder()
                .occurredAt(Instant.now())
                .username(username)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .clientIp(ip)
                .userAgent(ua)
                .build());
    }

    @Transactional(readOnly = true)
    public List<DataAccessAudit> recent() {
        return auditRepository.findTop200ByOrderByOccurredAtDesc();
    }
}
