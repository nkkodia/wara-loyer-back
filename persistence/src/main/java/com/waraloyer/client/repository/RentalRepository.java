package com.waraloyer.client.repository;

import com.waraloyer.client.model.Rental;
import io.micrometer.observation.ObservationFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByUserId(Long userId);

    Long countByUserIdAndStatus(Long userId, String status);

    @Query("SELECT SUM(r.amountDue) FROM Rental r WHERE r.user.id = :userId AND r.status = :status")
    BigDecimal sumAmountDueByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Query("SELECT SUM(r.amountDue) FROM Rental r WHERE r.user.id = :userId AND r.dueDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountDueByUserIdAndDueDateBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    List<Rental> findByTenantIdAndUserId(Long tenantId, Long userId);

    Optional<Rental> findByTenantId(Long tenantId);

    // Automatically generated query to find the single most recent rental for a property.
    Optional<Rental> findTopByPropertyIdOrderByDueDateDesc(Long propertyId);

}
