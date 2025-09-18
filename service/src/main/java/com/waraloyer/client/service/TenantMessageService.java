package com.waraloyer.client.service;

import com.waraloyer.client.model.Tenant;
import com.waraloyer.client.model.TenantMessageLog;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.TenantMessageLogRepository;
import com.waraloyer.client.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TenantMessageService {

    private final TenantMessageLogRepository tenantMessageLogRepository;
    private final RentalService rentalService;


    @Autowired
    public TenantMessageService(TenantMessageLogRepository tenantMessageLogRepository, RentalService rentalService ) {
        this.tenantMessageLogRepository = tenantMessageLogRepository;
        this.rentalService = rentalService;
    }


    /**
     * Récupère la liste des messages reçus par un utilisateur (bailleur).
     * @param userId L'ID de l'utilisateur.
     * @return La liste des messages.
     */
    public List<TenantMessageLog> getMessagesByUserId(Long userId) {
        return tenantMessageLogRepository.findByUserId(userId);
    }

    /**
     * Récupère les messages des locataires pour un bien spécifique,
     * en vérifiant que le bien appartient à l'utilisateur actuel.
     * @return La liste des messages pour ce bien.
     */
    public List<TenantMessageLog> findByRentalId(Long rentalId, User currentUser) {
        if (!rentalService.belongsToUser(rentalId, currentUser.getId())) {
            throw new AccessDeniedException("Accès refusé. Ce loyer n'appartient pas à cet utilisateur.");
        }
        // Use the new repository method
        return tenantMessageLogRepository.findByRental_Id(rentalId);
    }
}
