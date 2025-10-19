package com.medcloud.app.domain.dto;

import com.medcloud.app.domain.enums.DocumentType;
import com.medcloud.app.domain.enums.RoleName;

import java.time.LocalDate;

public record UserRequestCreate(
        String username,
        String email,
        String password,
        RoleName role,
        DocumentType documentType,
        String documentNumber,
        String fullName,
        LocalDate birthDate,
        // Campos de Doctor
        String specialty,
        String licenseNumber,
        //Campos de paciente
        Integer weight
) {
}
