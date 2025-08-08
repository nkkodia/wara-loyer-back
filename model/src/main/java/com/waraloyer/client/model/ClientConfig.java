package com.waraloyer.client.model;


// Entité pour la configuration client

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "client_config")
@Data
public class ClientConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne // Un utilisateur a une seule configuration
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    private String ownerEmail;
    private String smsReminderMessage;
    private String smsRelanceMessage;
    private Integer reminderDaysBefore;
    private Integer relanceDaysAfter;
    private String defaultPaymentMethod;
    private String ribDetails;
    private String mobileMoneyLink;
    private String contactPersonDetails;
}