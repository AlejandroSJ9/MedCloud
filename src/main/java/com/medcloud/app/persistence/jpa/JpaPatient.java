package com.medcloud.app.persistence.jpa;

import com.medcloud.app.persistence.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaPatient extends JpaRepository<PatientEntity, UUID> {
    Optional<PatientEntity> findByDocumentNumber(String documentNumber);
}
