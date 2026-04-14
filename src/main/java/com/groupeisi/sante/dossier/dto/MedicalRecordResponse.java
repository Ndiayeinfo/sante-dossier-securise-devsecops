package com.groupeisi.sante.dossier.dto;

import java.time.Instant;

public record MedicalRecordResponse(
        Long id,
        Long patientId,
        String summary,
        String clinicalNotes,
        String nursingNotes,
        Instant createdAt,
        String createdByUsername
) {
}
