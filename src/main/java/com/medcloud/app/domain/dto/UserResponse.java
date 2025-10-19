package com.medcloud.app.domain.dto;

import com.medcloud.app.domain.enums.RoleName;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        Set<RoleName> roles,
        String documentType,
        String documentNumber,
        String fullName,
        LocalDate birthDate,
        DoctorResponse doctorProfile,
        PatientResponse patientProfile
) {
}
