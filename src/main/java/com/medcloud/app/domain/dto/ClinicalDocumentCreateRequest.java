package com.medcloud.app.domain.dto;

import com.medcloud.app.domain.enums.DocumentKind;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.time.LocalDate;

/**
 * DTO de entrada (Request) para la subida de un nuevo documento usando Base64.
 * Ahora incluye datos del paciente para crearlo automáticamente si no existe.
 */
@Value
public class ClinicalDocumentCreateRequest {

    // Datos del paciente (para crear si no existe)
    @NotBlank(message = "La cédula del paciente es requerida.")
    String patientDocumentNumber;

    @Size(max=120, message = "El nombre no puede exceder los 120 caracteres.")
    String patientFullName;

    LocalDate patientBirthDate;

    @Size(max=1000, message = "El tratamiento no puede exceder los 1000 caracteres.")
    String patientTreatment;

    @NotNull(message = "El estado del diagnóstico es requerido.")
    Boolean patientDiagnosisInProgress;

    @NotBlank(message = "El ID de la EPS que sube es requerido.")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "El ID de la EPS debe ser un UUID válido.")
    String uploadedByEpsId;

    // Datos del médico que carga la historia clínica
    @Size(max=120, message = "El nombre del médico no puede exceder los 120 caracteres.")
    String doctorName;

    @NotBlank(message = "La cédula del médico es requerida.")
    String doctorDocumentNumber;

    String doctorSpecialty;

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
