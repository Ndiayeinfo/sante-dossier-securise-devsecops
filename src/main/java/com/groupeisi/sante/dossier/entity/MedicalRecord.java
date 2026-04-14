package com.groupeisi.sante.dossier.entity;

import com.groupeisi.sante.dossier.crypto.EncryptedStringConverter;
import com.groupeisi.sante.dossier.domain.DataSensitivity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "medical_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    /** Résumé visible selon rôle (soignant). */
    @Column(length = 500)
    private String summary;

    /** Notes cliniques — chiffré au repos ({@link DataSensitivity#DONNEES_MEDICALES}), accès médecin. */
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 8192)
    private String clinicalNotes;

    /** Notes infirmières — texte en clair en MVP ; peut être chiffré en évolution. */
    @Column(length = 4096)
    private String nursingNotes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(length = 128)
    private String createdByUsername;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
