package com.waraloyer.client.repository;

import com.waraloyer.client.model.ClientConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientConfigRepository extends JpaRepository<ClientConfig, Long> {
    Optional<ClientConfig> findByUserId(Long userId);
}