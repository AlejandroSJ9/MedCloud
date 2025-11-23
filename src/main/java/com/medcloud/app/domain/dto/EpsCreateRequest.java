package com.medcloud.app.domain.dto;

public record EpsCreateRequest(
        String username,
        String email,
        String password,
        String epsName,
        String nit
) {
}