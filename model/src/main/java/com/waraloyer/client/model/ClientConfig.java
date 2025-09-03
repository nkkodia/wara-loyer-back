package com.waraloyer.client.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore // <-- Ignore le champ lors de la sérialisation
    private User user;
}
