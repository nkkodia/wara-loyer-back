package com.waraloyer.client.repository;

// src/main/java/com/waraloyer/client/repository/PasswordResetTokenRepository.java

import com.waraloyer.client.model.PasswordResetToken;
import com.waraloyer.client.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    void deleteByUser(User user);
}
