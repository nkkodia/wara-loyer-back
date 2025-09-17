package com.waraloyer.client.repository;

import com.waraloyer.client.model.TenantMessageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<TenantMessageLog> findByTenant_Id(Long tenantId);
    List<TenantMessageLog> findByRental_Id(Long rentalId);
    @Query("SELECT tml FROM TenantMessageLog tml WHERE tml.property.id = :propertyId AND tml.user.id = :userId")
    List<TenantMessageLog> findByPropertyIdAndUserId(@Param("propertyId") Long propertyId, @Param("userId") Long userId);
}
