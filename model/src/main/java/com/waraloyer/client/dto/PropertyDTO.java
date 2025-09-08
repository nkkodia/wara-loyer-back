package com.waraloyer.client.dto;

import com.waraloyer.client.model.Property;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PropertyDTO {
    private Long id;
    private String name;
    private String address;
    private String type;
    private BigDecimal rentAmount;
    private BigDecimal chargesAmount;
    private String description;
    private Integer rentPaymentDate;
    private Long userId;

    public PropertyDTO(Property property) {
        this.id = property.getId();
        this.name = property.getName();
        this.address = property.getAddress();
        this.type = property.getType();
        this.rentAmount = property.getRentAmount();
        this.chargesAmount = property.getChargesAmount();
        this.description = property.getDescription();
        this.rentPaymentDate = property.getRentPaymentDate();
        this.userId = property.getUser().getId();
    }
}