package com.medcloud.app.domain.service;

import com.medcloud.app.domain.dto.ClinicalDocumentCreateRequest;
import com.medcloud.app.domain.dto.ClinicalDocumentDto;
import com.medcloud.app.domain.exceptions.InvalidUuidException;
import com.medcloud.app.domain.exceptions.PatientAlreadyInProgressException;
import com.medcloud.app.domain.exceptions.ResourceNotFoundException;
import com.medcloud.app.persistence.mapper.ClinicalDocumentMapper;
import com.medcloud.app.persistence.entity.ClinicalDocument;
import com.medcloud.app.persistence.entity.PatientEntity;
import com.medcloud.app.persistence.entity.EpsEntity;
import com.medcloud.app.domain.repository.ClinicalDocumentRepository;
import com.medcloud.app.domain.repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.EntityManager;

@Service
@RequiredArgsConstructor
public class ClinicalDocumentServiceImpl {

    private final ClinicalDocumentRepository documentRepository;
    private final ClinicalDocumentMapper documentMapper;
    private final PatientRepository patientRepository;
    private final EntityManager entityManager;

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
        // 1. Buscar o crear paciente por cédula
        PatientEntity patient = findOrCreatePatient(requestDto);

        // 2. Validar y obtener EPS
        String epsIdString = requestDto.getUploadedByEpsId().toLowerCase();
        UUID epsUuid;
        try {
            epsUuid = UUID.fromString(epsIdString);
        } catch (IllegalArgumentException e) {
            throw new InvalidUuidException("UUID de EPS inválido: " + e.getMessage());
        }

        EpsEntity eps = entityManager.find(EpsEntity.class, epsUuid);
        if (eps == null) {
            throw new ResourceNotFoundException("EPS con ID " + epsUuid + " no encontrada");
        }

        // 3. Mapear DTO a Entidad
        ClinicalDocument document = documentMapper.toEntity(requestDto);

        // 4. Asignar relaciones y metadatos calculados
        document.setPatient(patient);
        document.setUploadedBy(eps);

        // Asignar datos del médico
        document.setDoctorName(requestDto.getDoctorName());
        document.setDoctorDocumentNumber(requestDto.getDoctorDocumentNumber());
        document.setDoctorSpecialty(requestDto.getDoctorSpecialty());

        long calculatedSize = calculateBinarySizeBytes(requestDto.getFileContentBase64());
        document.setSizeBytes(calculatedSize);

        // 5. Guardar en la DB
        ClinicalDocument savedDocument = documentRepository.save(document);

        // 6. Mapear y devolver el DTO de salida
        return documentMapper.toDto(savedDocument);
    }

    /**
     * Busca un paciente por cédula, si no existe lo crea con los datos del request.
     * Valida que no haya conflicto con otras EPS si el diagnóstico está en curso.
     */
    private PatientEntity findOrCreatePatient(ClinicalDocumentCreateRequest requestDto) {
        Optional<PatientEntity> existingPatient = patientRepository.findByDocumentNumber(requestDto.getPatientDocumentNumber());

        if (existingPatient.isPresent()) {
            PatientEntity patient = existingPatient.get();

            // Si el paciente ya existe y tiene diagnóstico en curso, verificar que sea la misma EPS
            if (patient.isDiagnosisInProgress()) {
                // Verificar si alguna EPS ya tiene este paciente en tratamiento
                // Para una implementación completa, necesitaríamos una tabla de relación EPS-Paciente
                // Por ahora, lanzamos excepción si ya está en curso
                throw new PatientAlreadyInProgressException("El paciente ya tiene un diagnóstico en curso con otra EPS");
            }

            // Actualizar datos del paciente si es necesario
            patient.setFullName(requestDto.getPatientFullName());
            patient.setBirthDate(requestDto.getPatientBirthDate());
            patient.setTreatment(requestDto.getPatientTreatment());
            patient.setDiagnosisInProgress(requestDto.getPatientDiagnosisInProgress());

            return patientRepository.save(patient);
        }

        // Crear nuevo paciente
        PatientEntity newPatient = new PatientEntity();
        newPatient.setDocumentNumber(requestDto.getPatientDocumentNumber());
        newPatient.setFullName(requestDto.getPatientFullName());
        newPatient.setBirthDate(requestDto.getPatientBirthDate());
        newPatient.setTreatment(requestDto.getPatientTreatment());
        newPatient.setDiagnosisInProgress(requestDto.getPatientDiagnosisInProgress());

        return patientRepository.save(newPatient);
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
     * Obtiene todos los documentos clínicos asociados a un paciente por su cédula.
     */
    public List<ClinicalDocumentDto> getDocumentsByPatientDocumentNumber(String documentNumber) {
        Optional<PatientEntity> patientOpt = patientRepository.findByDocumentNumber(documentNumber);

        if (patientOpt.isEmpty()) {
            return List.of(); // Retorna lista vacía si el paciente no existe
        }

        PatientEntity patient = patientOpt.get();
        List<ClinicalDocument> documents = documentRepository.findByPatientId(patient.getId());

        return documentMapper.toDtoList(documents);
    }
}