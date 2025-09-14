package com.waraloyer.client.repository;

import com.waraloyer.client.model.TenantMessageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantMessageLogRepository extends JpaRepository<TenantMessageLog, Long> {
    /**
     * Récupère la liste des messages pour un utilisateur (bailleur) spécifique.
     * @param userId L'ID de l'utilisateur (bailleur).
     * @return La liste des messages.
     */
    List<TenantMessageLog> findByUserId(Long userId);
    List<TenantMessageLog> findByTenantId(Long tenantId);
    List<TenantMessageLog> findByRentalId(Long rentalId);
}
