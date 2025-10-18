package com.medcloud.app.domain.dto;

public record GeneralResponse(
        boolean succes,
        String adicionalInfo,
        UserResponse userResponse
) {
}
