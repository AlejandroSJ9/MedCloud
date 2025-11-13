package com.medcloud.app.persistence.repositoryimp;

import com.medcloud.app.domain.repository.PatientRepository;
import com.medcloud.app.persistence.entity.PatientEntity;
import com.medcloud.app.persistence.jpa.JpaPatient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del contrato de PatientRepository (Dominio).
 * Clase Adapter que interactúa con Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class PatientRepositoryImp implements PatientRepository {

    // Se asume que JpaPatient extiende JpaRepository<PatientEntity, UUID>
    private final JpaPatient jpaPatient;

    @Override
    public Optional<PatientEntity> findById(UUID uuid) {
        return jpaPatient.findById(uuid);
    }

    @Override
    public Optional<PatientEntity> findByDocumentNumber(String documentNumber) {
        return jpaPatient.findByDocumentNumber(documentNumber);
    }

    @Override
    public PatientEntity save(PatientEntity patient) {
        return jpaPatient.save(patient);
    }
}
