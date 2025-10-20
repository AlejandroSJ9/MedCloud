package com.medcloud.app.domain.repository;

import com.medcloud.app.persistence.entity.PatientEntity;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrato (puerto de salida) de dominio para la entidad Patient.
 * La implementación de persistencia (PatientRepositoryImp) debe cumplir con este contrato.
 */
public interface PatientRepository {
    /**
     * Busca una entidad Patient por su UUID.
     * @param uuid El ID del paciente.
     * @return Un Optional que contiene la entidad si se encuentra.
     */
    Optional<PatientEntity> findById(UUID uuid);
    // Puedes añadir otros métodos de dominio aquí si son necesarios
}