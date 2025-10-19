package com.medcloud.app.domain.service;

import com.medcloud.app.domain.exceptions.WeakPasswordException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordHasher {
    private final PasswordEncoder encoder;

    public PasswordHasher() {
        this.encoder = new BCryptPasswordEncoder();
    }

    public String hash(String rawPassword) {
        return this.encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String hash) {
        return this.encoder.matches(rawPassword, hash);
    }

    public void validateStrength(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new WeakPasswordException("La contraseña debe tener al menos 8 caracteres.");
        }
        if (!rawPassword.matches(".*[A-Z].*")) {
            throw new WeakPasswordException("La contraseña debe contener al menos una letra mayúscula.");
        }
        if (!rawPassword.matches(".*[a-z].*")) {
            throw new WeakPasswordException("La contraseña debe contener al menos una letra minúscula.");
        }
        if (!rawPassword.matches(".*\\d.*")) {
            throw new WeakPasswordException("La contraseña debe contener al menos un número.");
        }
        if (!rawPassword.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            throw new WeakPasswordException("La contraseña debe contener al menos un carácter especial.");
        }
    }
}
