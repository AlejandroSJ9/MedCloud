package com.medcloud.app.persistence.jpa;

import com.medcloud.app.persistence.entity.ClinicalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio de Spring Data JPA.
 *
 * Extiende JpaRepository, usando ClinicalDocument como entidad y UUID como tipo de ID.
 * Proporciona métodos CRUD automáticos.
 */
@Repository
public interface ClinicalDocumentJpaRepository extends JpaRepository<ClinicalDocument, UUID> {

    /**
     * Consulta personalizada para la lógica de negocio:
     * Busca todos los documentos asociados a un ID de paciente específico.
     * Spring Data JPA infiere la consulta automáticamente por el nombre del método.
     * @param patientId El UUID del paciente.
     * @return Una lista de documentos clínicos.
     */
    List<ClinicalDocument> findByPatientId(UUID patientId);
}
