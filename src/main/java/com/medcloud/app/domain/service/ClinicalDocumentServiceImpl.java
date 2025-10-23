package com.medcloud.app.domain.service;

import com.medcloud.app.domain.dto.ClinicalDocumentCreateRequest;
import com.medcloud.app.domain.dto.ClinicalDocumentDto;
import com.medcloud.app.domain.exceptions.InvalidUuidException;
import com.medcloud.app.domain.exceptions.ResourceNotFoundException;
import com.medcloud.app.persistence.mapper.ClinicalDocumentMapper;
import com.medcloud.app.persistence.entity.ClinicalDocument;
import com.medcloud.app.persistence.entity.PatientEntity;
import com.medcloud.app.persistence.entity.UserEntity;
import com.medcloud.app.domain.repository.ClinicalDocumentRepository;
import com.medcloud.app.domain.repository.PatientRepository;
import com.medcloud.app.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import jakarta.persistence.EntityManager; // Importamos EntityManager

@Service
@RequiredArgsConstructor
public class ClinicalDocumentServiceImpl {

    private final ClinicalDocumentRepository documentRepository;
    private final ClinicalDocumentMapper documentMapper;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository; // Se mantiene, aunque lo usaremos menos
    private final EntityManager entityManager; // 🚨 ¡Nuevo: Inyección de EntityManager!

    /**
     * Calcula el tamaño binario real (en bytes) a partir de una cadena Base64.
     */
    private long calculateBinarySizeBytes(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return 0;
        }
        try {
            return java.util.Base64.getDecoder().decode(base64String).length;
        } catch (IllegalArgumentException e) {
            System.err.println("Error decodificando Base64 para calcular el tamaño: " + e.getMessage());
            return 0;
        }
    }

    @Transactional
    public ClinicalDocumentDto uploadDocument(ClinicalDocumentCreateRequest requestDto) {
        // 1. Validar y obtener entidades de clave foránea

        // Normalización a minúsculas para robustez del UUID
        String patientIdString = requestDto.getPatientId().toLowerCase();
        String userIdString = requestDto.getUploadedByUserId().toLowerCase();

        UUID patientUuid;
        UUID userUuid;
        try {
            patientUuid = UUID.fromString(patientIdString);
            userUuid = UUID.fromString(userIdString);
        } catch (IllegalArgumentException e) {
            throw new InvalidUuidException("UUID inválido proporcionado: " + e.getMessage());
        }

        // 🚨 CAMBIO DE ESTRATEGIA: Uso de EntityManager.find() para forzar la lectura DB.
        // Esto ignora el caché de nivel 1 de Hibernate que podría estar obsoleto.

        // Buscar Paciente
        PatientEntity patient = entityManager.find(PatientEntity.class, patientUuid);
        if (patient == null) {
            throw new ResourceNotFoundException("Paciente con ID " + patientUuid + " no encontrado");
        }

        // Buscar Usuario
        UserEntity user = entityManager.find(UserEntity.class, userUuid);
        if (user == null) {
            throw new ResourceNotFoundException("Usuario con ID " + userUuid + " no encontrado");
        }

        // 2. Mapear DTO a Entidad
        ClinicalDocument document = documentMapper.toEntity(requestDto);

        // 3. Asignar relaciones y metadatos calculados
        document.setPatient(patient);
        document.setUploadedBy(user);

        long calculatedSize = calculateBinarySizeBytes(requestDto.getFileContentBase64());
        document.setSizeBytes(calculatedSize);

        // 4. Guardar en la DB
        ClinicalDocument savedDocument = documentRepository.save(document);

        // 5. Mapear y devolver el DTO de salida
        return documentMapper.toDto(savedDocument);
    }

    /**
     * Obtiene un documento clínico por su ID.
     */
    public ClinicalDocumentDto getDocumentById(String idString) {
        UUID uuid;
        try {
            uuid = UUID.fromString(idString);
        } catch (IllegalArgumentException e) {
            throw new InvalidUuidException("UUID inválido proporcionado: " + e.getMessage());
        }

        ClinicalDocument document = documentRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Documento clínico con ID " + idString + " no encontrado."));

        return documentMapper.toDto(document);
    }

    /**
     * Obtiene todos los documentos clínicos asociados a un paciente.
     */
    public List<ClinicalDocumentDto> getDocumentsByPatientId(String patientIdString) {
        UUID patientUuid;
        try {
            patientUuid = UUID.fromString(patientIdString);
        } catch (IllegalArgumentException e) {
            throw new InvalidUuidException("UUID inválido proporcionado: " + e.getMessage());
        }

        List<ClinicalDocument> documents = documentRepository.findByPatientId(patientUuid);

        return documentMapper.toDtoList(documents);
    }
}