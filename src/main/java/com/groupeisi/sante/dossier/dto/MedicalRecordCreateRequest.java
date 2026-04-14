package com.groupeisi.sante.dossier.dto;

import jakarta.validation.constraints.Size;

public record MedicalRecordCreateRequest(
        @Size(max = 500) String summary,
        @Size(max = 8000) String clinicalNotes,
        @Size(max = 4000) String nursingNotes
) {
}
