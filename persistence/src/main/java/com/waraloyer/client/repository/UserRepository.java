package com.waraloyer.client.repository;

import com.waraloyer.client.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
    List<User> findAllByEnabledTrue()
            ;
    @Query("SELECT u.subscription.maxPropertiesLimit FROM User u WHERE u.id = :userId")
    Optional<Integer> findMaxPropertiesLimitByUserId(@Param("userId") Long userId);

    @Query("SELECT u.subscription.monthlySmsLimit FROM User u WHERE u.id = :userId")
    Optional<Integer> findMonthlySmsLimitByUserId(@Param("userId") Long userId);
}
