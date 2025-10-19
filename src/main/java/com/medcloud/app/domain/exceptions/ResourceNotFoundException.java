package com.medcloud.app.domain.exceptions;

/**
 * Excepción de Dominio para manejar recursos no encontrados (código HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
