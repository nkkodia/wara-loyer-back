package com.waraloyer.client.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "client_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ownerEmail;
    private String smsReminderMessage;
    private String smsRelanceMessage;
    private Integer reminderDaysBefore;
    private Integer relanceDaysAfter;
    private String defaultPaymentMethod;
    private String ribDetails;
    private String mobileMoneyLink;
    private String contactPersonDetails;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
