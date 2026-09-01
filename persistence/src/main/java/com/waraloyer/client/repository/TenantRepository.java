package com.waraloyer.client.repository;

import com.waraloyer.client.model.Tenant;
import com.waraloyer.client.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    List<Tenant> findByUserId(Long userId);

    Long countByUserId(Long id);
    /**
     * Finds all active tenants for a specific user.
     * Assuming you have an 'enabled' field on your Tenant entity.
     * @param user The user object.
     * @param enabled The status of the tenant (true for active).
     * @return A list of active tenants.
     */
    List<Tenant> findByUserAndEnabled(User user, boolean enabled);
}
