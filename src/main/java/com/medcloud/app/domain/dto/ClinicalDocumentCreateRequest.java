package com.medcloud.app.domain.dto;

import com.medcloud.app.domain.enums.DocumentKind;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Value;

/**
 * DTO de entrada (Request) para la subida de un nuevo documento usando Base64.
 */
@Value
public class ClinicalDocumentCreateRequest {

    @NotBlank(message = "El ID del paciente es requerido.")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "El ID del paciente debe ser un UUID válido.")
    String patientId;

    @NotBlank(message = "El ID del usuario que sube es requerido.")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "El ID del usuario debe ser un UUID válido.")
    String uploadedByUserId;

    @NotNull(message = "El tipo de documento es requerido.")
    DocumentKind kind;

    @NotBlank
    @Size(max=200, message = "El nombre del archivo no puede exceder los 200 caracteres.")
    String filename;

    @NotBlank(message = "El contenido Base64 del archivo es requerido.")
    String fileContentBase64; // El contenido del archivo

    @Size(max=120, message = "El tipo MIME no puede exceder los 120 caracteres.")
    String mimeType;

    @Min(value = 1, message = "El tamaño del archivo debe ser mayor a 0 bytes.")
    long sizeBytes; // El servicio recalculará esto para el tamaño binario real
}
