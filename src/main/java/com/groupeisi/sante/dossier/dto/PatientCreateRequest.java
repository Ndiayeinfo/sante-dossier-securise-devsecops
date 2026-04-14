package com.groupeisi.sante.dossier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientCreateRequest(
        @NotBlank @Size(max = 120) String firstName,
        @NotBlank @Size(max = 120) String lastName,
        LocalDate birthDate,
        @Size(max = 64) String coverageIdentifier
) {
}
