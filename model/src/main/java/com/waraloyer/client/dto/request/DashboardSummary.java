package com.waraloyer.client.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
    private Long totalProperties;
    private Long totalTenants;
    private Long totalUnpaidRentals;
    private BigDecimal totalRentalsAmount;
    private BigDecimal totalUnpaidAmount;
    private Long smsSentInMonth;
}