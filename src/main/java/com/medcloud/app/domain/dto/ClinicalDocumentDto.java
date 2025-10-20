package com.medcloud.app.domain.dto;

import com.medcloud.app.domain.enums.DocumentKind;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

/**
 * DTO de salida / Modelo de dominio para ClinicalDocument, incluye el contenido Base64.
 */
@Value
@Builder
public class ClinicalDocumentDto {
    String id;
    String patientId;
    String uploadedByUserId;
    DocumentKind kind;
    String filename;
    String fileContentBase64; // El contenido Base64 que se devuelve
    String mimeType;
    long sizeBytes;
    OffsetDateTime uploadedAt;
}
