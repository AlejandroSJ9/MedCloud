package com.medcloud.app.domain.repository;

import com.medcloud.app.persistence.entity.ClinicalDocument;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrato (puerto de salida) para la capa de Dominio.
 * Define las operaciones CRUD que el Service necesita, trabajando con la entidad de persistencia (ClinicalDocument).
 */
public interface ClinicalDocumentRepository {

    /**
     * Guarda la entidad ClinicalDocument en la persistencia.
     * @param entity La entidad JPA a guardar.
     * @return La entidad JPA guardada.
     */
    ClinicalDocument save(ClinicalDocument entity);

    Optional<ClinicalDocument> findById(UUID id);
    List<ClinicalDocument> findByPatientId(UUID patientId);
    void deleteById(UUID id);
}
