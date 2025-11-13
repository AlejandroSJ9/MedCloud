package com.medcloud.app.domain.exceptions;

public class PatientAlreadyInProgressException extends RuntimeException {
    public PatientAlreadyInProgressException(String message) {
        super(message);
    }
}