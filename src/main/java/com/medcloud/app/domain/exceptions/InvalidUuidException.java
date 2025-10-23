package com.medcloud.app.domain.exceptions;

/**
 * Excepción de Dominio para manejar UUIDs inválidos (código HTTP 400).
 */
public class InvalidUuidException extends RuntimeException {
    public InvalidUuidException(String message) {
        super(message);
    }
}