package com.groupeisi.sante.dossier.entity;

import com.groupeisi.sante.dossier.crypto.EncryptedStringConverter;
import com.groupeisi.sante.dossier.domain.DataSensitivity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String firstName;

    @Column(nullable = false, length = 120)
    private String lastName;

    private LocalDate birthDate;

    /** NIR / n° couverture — chiffré au repos ({@link DataSensitivity#IDENTIFIANTS_COUVERTURE}). */
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 2048)
    private String coverageIdentifier;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
