package com.medcloud.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EpsValidationResponseDTO(
        boolean isValid,
        String epsName,
        String numeroDocumento,
        String message
) {
}