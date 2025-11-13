package com.medcloud.app.domain.service;

import com.medcloud.app.domain.repository.PatientRepository;
import com.medcloud.app.persistence.entity.PatientEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Optional<PatientEntity> findByDocumentNumber(String documentNumber) {
        return patientRepository.findByDocumentNumber(documentNumber);
    }
}