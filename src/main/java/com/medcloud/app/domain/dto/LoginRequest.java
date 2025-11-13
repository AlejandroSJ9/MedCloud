package com.medcloud.app.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Identifier (email or username) is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}