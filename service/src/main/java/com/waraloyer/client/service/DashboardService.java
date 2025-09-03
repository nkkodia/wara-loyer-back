package com.waraloyer.client.service;

import com.waraloyer.client.dto.request.DashboardSummary;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.PropertyRepository;
import com.waraloyer.client.repository.RentalRepository;
import com.waraloyer.client.repository.SmsLogRepository;
import com.waraloyer.client.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;


@Service
public class DashboardService {

    private final PropertyRepository propertyRepository;
    private final RentalRepository rentalRepository;
    private final TenantRepository tenantRepository;
    private final SmsLogRepository smsLogRepository;

    @Autowired
    public DashboardService(PropertyRepository propertyRepository, RentalRepository rentalRepository, TenantRepository tenantRepository, SmsLogRepository smsLogRepository) {
        this.propertyRepository = propertyRepository;
        this.rentalRepository = rentalRepository;
        this.tenantRepository = tenantRepository;
        this.smsLogRepository = smsLogRepository;
    }

    public DashboardSummary getSummaryForUser(User user) {
        // Compter les propriétés et les locataires de l'utilisateur
        Long totalProperties = propertyRepository.countByUserId(user.getId());
        Long totalTenants = tenantRepository.countByUserId(user.getId());

        // Calculer les loyers impayés et les montants
        Long totalUnpaidRentals = rentalRepository.countByUserIdAndStatus(user.getId(), "IMPAYÉ");
        BigDecimal totalUnpaidAmount = rentalRepository.sumAmountDueByUserIdAndStatus(user.getId(), "IMPAYÉ");

        // Calculer le total des loyers pour le mois en cours
        LocalDate startDateOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endDateOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
;

        BigDecimal totalRentalsAmount = rentalRepository.sumAmountDueByUserIdAndDueDateBetween(user.getId(), startDateOfMonth, endDateOfMonth);
        Long smsSentInMonth = smsLogRepository.countByUserIdAndSentDateBetween(user.getId(), startDateOfMonth, endDateOfMonth);

        // Créer l'objet de résumé et le retourner
        return new DashboardSummary(
                totalProperties,
                totalTenants,
                totalUnpaidRentals,
                totalRentalsAmount != null ? totalRentalsAmount : BigDecimal.ZERO,
                totalUnpaidAmount != null ? totalUnpaidAmount : BigDecimal.ZERO,
                smsSentInMonth
        );
    }
}