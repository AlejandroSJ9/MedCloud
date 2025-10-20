package com.medcloud.app.web.controller;

import com.medcloud.app.domain.dto.ClinicalDocumentCreateRequest;
import com.medcloud.app.domain.dto.ClinicalDocumentDto;
import com.medcloud.app.domain.service.ClinicalDocumentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     */
    @PostMapping
    public ResponseEntity<ClinicalDocumentDto> uploadDocument(@Valid @RequestBody ClinicalDocumentCreateRequest request) {
        ClinicalDocumentDto savedDocument = clinicalDocumentService.uploadDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);
    }

    /**
     * Obtiene un documento clínico por su ID (UUID).
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<ClinicalDocumentDto> getDocumentById(@PathVariable String documentId) {
        ClinicalDocumentDto document = clinicalDocumentService.getDocumentById(documentId);
        return ResponseEntity.ok(document);
    }

    /**
     * Obtiene todos los documentos clínicos asociados a un paciente (por su ID UUID).
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ClinicalDocumentDto>> getDocumentsByPatientId(@PathVariable String patientId) {
        List<ClinicalDocumentDto> documents = clinicalDocumentService.getDocumentsByPatientId(patientId);
        return ResponseEntity.ok(documents);
    }

    // Nota: Es recomendable añadir un ControllerAdvice para manejar la ResourceNotFoundException (404).
}
