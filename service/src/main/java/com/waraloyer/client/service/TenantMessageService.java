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
    private final TenantRepository tenantRepository;
    private final RentalService rentalService;

    @Autowired
    public TenantMessageService(TenantMessageLogRepository tenantMessageLogRepository, TenantRepository tenantRepository, RentalService rentalService) {
        this.tenantMessageLogRepository = tenantMessageLogRepository;
        this.tenantRepository = tenantRepository;
        this.rentalService = rentalService;
    }

    /**
     * Permet à un locataire de signaler un problème. Cet endpoint est public.
     * @param tenantId L'ID du locataire.
     * @param message Le message du locataire.
     * @return Le log du message sauvegardé.
     */
    public TenantMessageLog reportProblem(Long tenantId, String message) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Locataire non trouvé."));

        TenantMessageLog messageLog = new TenantMessageLog();
        messageLog.setTenant(tenant);
        messageLog.setUser(tenant.getUser()); // Associe le message au bailleur
        messageLog.setProperty(tenant.getProperty()); // Associe le message au bien
        messageLog.setMessage(message);
        messageLog.setSentDate(Instant.now());
        messageLog.setStatus("SENT"); // Le message est envoyé

        return tenantMessageLogRepository.save(messageLog);
    }

    /**
     * Récupère la liste des messages reçus par un utilisateur (bailleur).
     * @param userId L'ID de l'utilisateur.
     * @return La liste des messages.
     */
    public List<TenantMessageLog> getMessagesByUserId(Long userId) {
        return tenantMessageLogRepository.findByUserId(userId);
    }

    public List<TenantMessageLog> findByTenantId(Long tenantId, User currentUser) {
        // Vérifie si le loyer associé au locataire appartient à l'utilisateur
        if (!rentalService.tenantBelongsToUser(tenantId, currentUser.getId())) {
            throw new AccessDeniedException("Accès refusé. Ce locataire n'appartient pas à cet utilisateur.");
        }
        return tenantMessageLogRepository.findByTenantId(tenantId);
    }

    public List<TenantMessageLog> findByRentalId(Long rentalId, User currentUser) {
        // Vérifie si le loyer appartient à l'utilisateur
        if (!rentalService.belongsToUser(rentalId, currentUser.getId())) {
            throw new AccessDeniedException("Accès refusé. Ce loyer n'appartient pas à cet utilisateur.");
        }
        return tenantMessageLogRepository.findByRentalId(rentalId);
    }
}
