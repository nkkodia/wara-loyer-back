package com.waraloyer.client.repository;

import com.waraloyer.client.model.SmsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmsLogRepository extends JpaRepository<SmsLog, Long> {
    List<SmsLog> findByUserIdAndRentalId(Long userId, String rentalId);
}