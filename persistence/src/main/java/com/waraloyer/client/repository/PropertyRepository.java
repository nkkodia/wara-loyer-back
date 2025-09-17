package com.waraloyer.client.repository;

import com.waraloyer.client.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repository pour les biens immobiliers
@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    // Nouvelle implémentation qui garantit qu'un utilisateur existe
    @Query("SELECT p FROM Property p WHERE p.user.id = :userId")
    List<Property> findByUserId(@Param("userId") Long userId);
    Long countByUserId(Long id);
}
