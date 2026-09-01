package com.waraloyer.client.dto;

import com.waraloyer.client.model.Property;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter

public class PropertyWithRentalInfoDTO extends PropertyDTO {
    private BigDecimal lastRentAmount;
    private LocalDate lastPaymentDate;

    public PropertyWithRentalInfoDTO() {
        super(new Property());
    }

    public PropertyWithRentalInfoDTO(Property property) {
        super(property);
    }
}
