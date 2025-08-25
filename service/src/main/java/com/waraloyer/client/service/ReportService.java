package com.waraloyer.client.service;

import com.waraloyer.client.model.Rental;
import com.waraloyer.client.repository.RentalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final RentalRepository rentalRepository;

    @Autowired
    public ReportService(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    /**
     * Obtient un aperçu financier pour un utilisateur spécifique.
     * @param userId L'ID de l'utilisateur pour lequel générer le rapport.
     * @return Un Map contenant les données du rapport.
     */
    public Map<String, Object> getFinancialOverview(Long userId) {
        List<Rental> allRentals = rentalRepository.findByUserId(userId);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long paidRentals = 0;
        long overdueRentals = 0;
        long pendingRentals = 0;

        for (Rental rental : allRentals) {
            switch (rental.getStatus()) {
                case "PAID":
                    totalRevenue = totalRevenue.add(rental.getAmountDue());
                    paidRentals++;
                    break;
                case "OVERDUE":
                    overdueRentals++;
                    break;
                case "PENDING":
                    pendingRentals++;
                    break;
                default:
                    break;
            }
        }

        Map<String, Object> report = new HashMap<>();
        report.put("totalRevenue", totalRevenue);
        report.put("paidRentalsCount", paidRentals);
        report.put("overdueRentalsCount", overdueRentals);
        report.put("pendingRentalsCount", pendingRentals);

        return report;
    }
}
