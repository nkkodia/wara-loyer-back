package com.waraloyer.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling // Active les tâches planifiées
public class WaraLoyerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WaraLoyerApplication.class, args);
    }
}