package com.waraloyer.client.repository;

import com.waraloyer.client.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repository pour les biens immobiliers
@Repository
public interface PropertyRepository extends JpaRepository<Property, String> {
    List<Property> findByUserId(Long userId);
}
