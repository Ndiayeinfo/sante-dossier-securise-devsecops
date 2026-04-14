package com.groupeisi.sante.dossier.dto;

import java.time.Instant;
import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String coverageIdentifier,
        Instant createdAt
) {
}
