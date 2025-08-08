package com.waraloyer.client.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// Entité pour les utilisateurs (propriétaires)
@Entity
@Table(name = "app_user")
@Data // Fournit les getters, setters, toString, etc. grâce à Lombok
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
}