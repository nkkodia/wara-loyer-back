// src/main/java/com/waraloyer/client/dto/SmsRequestDTO.java
package com.waraloyer.client.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SmsRequestDTO {
    private String toPhoneNumber;
    private String messageBody;
    private String type;
    private LocalDateTime scheduleDate;
}