package com.groupeisi.sante.dossier.service;

import com.groupeisi.sante.dossier.audit.AuditService;
import com.groupeisi.sante.dossier.dto.MedicalRecordCreateRequest;
import com.groupeisi.sante.dossier.dto.MedicalRecordResponse;
import com.groupeisi.sante.dossier.entity.MedicalRecord;
import com.groupeisi.sante.dossier.entity.Patient;
import com.groupeisi.sante.dossier.repository.MedicalRecordRepository;
import com.groupeisi.sante.dossier.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository recordRepository;
    private final PatientRepository patientRepository;
    private final AuditService auditService;
    private final SecurityRoleChecker roles;

    @Transactional
    public MedicalRecordResponse create(Long patientId, MedicalRecordCreateRequest req) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientService.NotFoundException("Patient introuvable"));

        String clinical = req.clinicalNotes();
        if (roles.isInfirmier() && !roles.isMedecin()) {
            if (clinical != null && !clinical.isBlank()) {
                throw new AccessDeniedException("Seul un médecin peut saisir des notes cliniques.");
            }
            clinical = null;
        }

        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();

        MedicalRecord entity = MedicalRecord.builder()
                .patient(patient)
                .summary(req.summary())
                .clinicalNotes(clinical)
                .nursingNotes(req.nursingNotes())
                .createdByUsername(username)
                .build();
        entity = recordRepository.save(entity);
        auditService.record("CREATE", "MedicalRecord", entity.getId());
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> listForPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new PatientService.NotFoundException("Patient introuvable");
        }
        auditService.record("LIST", "MedicalRecord", patientId);
        return recordRepository.findByPatient_IdOrderByCreatedAtDesc(patientId).stream()
                .map(this::toResponse)
                .toList();
    }

    private MedicalRecordResponse toResponse(MedicalRecord e) {
        String clinical = e.getClinicalNotes();
        if (!roles.canViewClinicalNotes()) {
            clinical = null;
        }
        return new MedicalRecordResponse(
                e.getId(),
                e.getPatient().getId(),
                e.getSummary(),
                clinical,
                e.getNursingNotes(),
                e.getCreatedAt(),
                e.getCreatedByUsername()
        );
    }
}
