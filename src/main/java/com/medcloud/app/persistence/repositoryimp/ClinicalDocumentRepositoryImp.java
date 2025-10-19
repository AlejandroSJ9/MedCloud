package com.medcloud.app.persistence.repositoryimp;

import com.medcloud.app.domain.repository.ClinicalDocumentRepository;
import com.medcloud.app.persistence.entity.ClinicalDocument;
import com.medcloud.app.persistence.jpa.ClinicalDocumentJpaRepository; // Asumiendo que esta es tu interfaz JpaRepository
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del contrato de ClinicalDocumentRepository (Dominio).
 * Clase Adapter que interactúa directamente con JpaRepository y usa Entidades JPA.
 */
@Repository
@RequiredArgsConstructor
public class ClinicalDocumentRepositoryImp implements ClinicalDocumentRepository {

    private final ClinicalDocumentJpaRepository jpaRepository;

    @Override
    public ClinicalDocument save(ClinicalDocument entity) {
        // La implementación solo llama al JpaRepository
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<ClinicalDocument> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ClinicalDocument> findByPatientId(UUID patientId) {
        // Asumiendo que tienes un método en ClinicalDocumentJpaRepository como findByPatientId(UUID id)
        // Si no existe, necesitarías añadirlo a esa interfaz.
        return jpaRepository.findByPatientId(patientId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
