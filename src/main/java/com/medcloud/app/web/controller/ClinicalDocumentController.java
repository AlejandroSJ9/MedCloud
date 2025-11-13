package com.medcloud.app.web.controller;

import com.medcloud.app.domain.dto.ClinicalDocumentCreateRequest;
import com.medcloud.app.domain.dto.ClinicalDocumentDto;
import com.medcloud.app.domain.service.ClinicalDocumentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST para la gestión de documentos clínicos.
 * Delega toda la lógica de negocio a ClinicalDocumentServiceImpl.
 * Inyecta la implementación del servicio (según solicitud).
 */
@RestController
@RequestMapping("/clinical-documents")
@RequiredArgsConstructor
public class ClinicalDocumentController {

    private final ClinicalDocumentServiceImpl clinicalDocumentService;

    /**
     * Sube un nuevo documento clínico.
     * Requiere rol EPS. Si el paciente no existe, se crea automáticamente.
     */
    @PostMapping
    @PreAuthorize("hasRole('EPS')")
    public ResponseEntity<ClinicalDocumentDto> uploadDocument(@Valid @RequestBody ClinicalDocumentCreateRequest request) {
        ClinicalDocumentDto savedDocument = clinicalDocumentService.uploadDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);
    }

    /**
     * Obtiene un documento clínico por su ID (UUID).
     * Requiere rol EPS o PACIENTE.
     */
    @GetMapping("/{documentId}")
    @PreAuthorize("hasRole('EPS') or hasRole('PACIENTE')")
    public ResponseEntity<ClinicalDocumentDto> getDocumentById(@PathVariable String documentId) {
        ClinicalDocumentDto document = clinicalDocumentService.getDocumentById(documentId);
        return ResponseEntity.ok(document);
    }

    /**
     * Obtiene todos los documentos clínicos asociados a un paciente (por su cédula).
     * Endpoint público para que pacientes puedan consultar sus documentos sin autenticación.
     */
    @GetMapping("/patient/{documentNumber}")
    public ResponseEntity<List<ClinicalDocumentDto>> getDocumentsByPatientDocumentNumber(@PathVariable String documentNumber) {
        List<ClinicalDocumentDto> documents = clinicalDocumentService.getDocumentsByPatientDocumentNumber(documentNumber);
        return ResponseEntity.ok(documents);
    }

    /**
     * Obtiene todos los documentos clínicos asociados a un paciente (por su UUID).
     * Endpoint público para que pacientes puedan consultar sus documentos sin autenticación.
     */
    @GetMapping("/patient/uuid/{patientId}")
    public ResponseEntity<List<ClinicalDocumentDto>> getDocumentsByPatientId(@PathVariable String patientId) {
        try {
            java.util.UUID uuid = java.util.UUID.fromString(patientId);
            List<ClinicalDocumentDto> documents = clinicalDocumentService.getDocumentsByPatientId(uuid);
            return ResponseEntity.ok(documents);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Nota: Es recomendable añadir un ControllerAdvice para manejar la ResourceNotFoundException (404).
}
