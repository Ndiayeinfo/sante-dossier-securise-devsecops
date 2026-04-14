package com.groupeisi.sante.dossier.repository;

import com.groupeisi.sante.dossier.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatient_IdOrderByCreatedAtDesc(Long patientId);
}
