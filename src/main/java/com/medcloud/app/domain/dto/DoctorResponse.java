package com.medcloud.app.domain.dto;

public record DoctorResponse(
        String specialty,
        String licenseNumber
) {
}
