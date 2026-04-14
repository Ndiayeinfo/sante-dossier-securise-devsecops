package com.groupeisi.sante.dossier.web;

import com.groupeisi.sante.dossier.dto.PatientCreateRequest;
import com.groupeisi.sante.dossier.dto.PatientResponse;
import com.groupeisi.sante.dossier.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Identité et couverture (données sensibles)")
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SECRETAIRE','MEDECIN')")
    @Operation(summary = "Créer un patient")
    public PatientResponse create(@Valid @RequestBody PatientCreateRequest request) {
        return patientService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDECIN','INFIRMIER','SECRETAIRE')")
    @Operation(summary = "Lister les patients (journalisation d'accès)")
    public List<PatientResponse> list() {
        return patientService.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDECIN','INFIRMIER','SECRETAIRE')")
    @Operation(summary = "Détail patient (moindre privilège : secrétaire sans n° de couverture en clair)")
    public PatientResponse get(@PathVariable Long id) {
        return patientService.getById(id);
    }
}
