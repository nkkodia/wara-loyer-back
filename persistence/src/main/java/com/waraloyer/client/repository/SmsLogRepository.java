package com.waraloyer.client.repository;

import com.waraloyer.client.model.SmsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SmsLogRepository extends JpaRepository<SmsLog, Long> {
    List<SmsLog> findByUserIdAndRentalId(Long userId, Long rentalId);

    List<SmsLog> findByUserId(Long userId);

    @Query("SELECT COUNT(s) FROM SmsLog s WHERE s.user.id = :userId AND s.sentDate BETWEEN :startDate AND :endDate")
    Long countByUserIdAndSentDateBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}