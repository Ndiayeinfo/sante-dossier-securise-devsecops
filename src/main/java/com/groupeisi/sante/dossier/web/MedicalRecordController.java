package com.groupeisi.sante.dossier.web;

import com.groupeisi.sante.dossier.dto.MedicalRecordCreateRequest;
import com.groupeisi.sante.dossier.dto.MedicalRecordResponse;
import com.groupeisi.sante.dossier.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}/records")
@RequiredArgsConstructor
@Tag(name = "Dossier médical", description = "Notes soignants, notes cliniques réservées au médecin")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MEDECIN','INFIRMIER')")
    @Operation(summary = "Ajouter une entrée au dossier")
    public MedicalRecordResponse create(
            @PathVariable Long patientId,
            @Valid @RequestBody MedicalRecordCreateRequest request) {
        return medicalRecordService.create(patientId, request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDECIN','INFIRMIER')")
    @Operation(summary = "Historique du dossier (notes cliniques visibles uniquement pour le médecin)")
    public List<MedicalRecordResponse> list(@PathVariable Long patientId) {
        return medicalRecordService.listForPatient(patientId);
    }
}
