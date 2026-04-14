package com.groupeisi.sante.dossier.service;

import com.groupeisi.sante.dossier.audit.AuditService;
import com.groupeisi.sante.dossier.dto.PatientCreateRequest;
import com.groupeisi.sante.dossier.dto.PatientResponse;
import com.groupeisi.sante.dossier.entity.Patient;
import com.groupeisi.sante.dossier.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final AuditService auditService;
    private final SecurityRoleChecker roles;

    @Transactional
    public PatientResponse create(PatientCreateRequest req) {
        Patient p = Patient.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .birthDate(req.birthDate())
                .coverageIdentifier(req.coverageIdentifier())
                .build();
        p = patientRepository.save(p);
        auditService.record("CREATE", "Patient", p.getId());
        return toResponse(p, false);
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> listAll() {
        auditService.record("LIST", "Patient", null);
        return patientRepository.findAll().stream()
                .map(p -> toResponse(p, roles.isSecretaireOnly()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PatientResponse getById(Long id) {
        Patient p = patientRepository.findById(id).orElseThrow(() -> new NotFoundException("Patient introuvable"));
        auditService.record("READ", "Patient", id);
        boolean maskCoverage = roles.isSecretaire() && !roles.isMedecin() && !roles.isInfirmier();
        return toResponse(p, maskCoverage);
    }

    private static PatientResponse toResponse(Patient p, boolean maskCoverage) {
        String cov = p.getCoverageIdentifier();
        if (maskCoverage && cov != null && !cov.isEmpty()) {
            cov = "••••••••";
        }
        return new PatientResponse(
                p.getId(),
                p.getFirstName(),
                p.getLastName(),
                p.getBirthDate(),
                cov,
                p.getCreatedAt()
        );
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}
