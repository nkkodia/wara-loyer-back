package com.waraloyer.client.repository;

import com.waraloyer.client.model.TenantMessageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantMessageLogRepository extends JpaRepository<TenantMessageLog, Long> {
    List<TenantMessageLog> findByRental_Id(Long rentalId);


}
