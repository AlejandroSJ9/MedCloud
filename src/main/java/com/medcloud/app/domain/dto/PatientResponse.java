package com.medcloud.app.domain.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String documentNumber,
        String fullName,
        LocalDate birthDate,
        String treatment,
        boolean diagnosisInProgress
) {
}
