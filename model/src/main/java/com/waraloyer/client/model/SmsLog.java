package com.waraloyer.client.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "sms_log")
@Data
public class SmsLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "rental_id", referencedColumnName = "id")
    private Rental rental;
    @ManyToOne
    @JoinColumn(name = "locataire_id", referencedColumnName = "id")
    private Tenant locataire;
    @ManyToOne
    @JoinColumn(name = "property_id", referencedColumnName = "id")
    private Property property;
    private String type;
    private String message;
    private LocalDateTime sentDate;
    private String status;
    private String toPhoneNumber;
}