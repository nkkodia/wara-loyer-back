// src/main/java/com/waraloyer/client/dto/UserCreatePasswordDTO.java

package com.waraloyer.client.dto;

import lombok.Data;

@Data
public class UserCreatePasswordDTO {
    private String email;
    private String password;
}