package com.medcloud.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CaptchaResponseDTO(
        String captchaImage,
        String sessionId,
        String message
) {
}