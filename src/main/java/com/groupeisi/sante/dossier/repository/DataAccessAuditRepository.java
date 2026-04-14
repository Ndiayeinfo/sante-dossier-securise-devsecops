package com.groupeisi.sante.dossier.repository;

import com.groupeisi.sante.dossier.entity.DataAccessAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataAccessAuditRepository extends JpaRepository<DataAccessAudit, Long> {
    List<DataAccessAudit> findTop200ByOrderByOccurredAtDesc();
}
