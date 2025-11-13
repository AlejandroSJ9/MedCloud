package com.medcloud.app.persistence.entity;

import com.medcloud.app.domain.enums.DocumentKind;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="clinical_documents",
        indexes = {
                @Index(name="ix_doc_patient", columnList="patient_id"),
                @Index(name="ix_doc_eps", columnList="uploaded_by_eps_id")
        })
public class ClinicalDocument extends BaseId{
    @ManyToOne(optional=false) @JoinColumn(name="patient_id", nullable=false)
    private PatientEntity patient;

    // Quién subió el documento (EPS)
    @ManyToOne(optional=false) @JoinColumn(name="uploaded_by_eps_id", nullable=false)
    private EpsEntity uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=10)
    private DocumentKind kind = DocumentKind.PDF;

    @NotBlank
    @Size(max=200)
    private String filename;

    /**
     * Campo para almacenar el contenido del archivo codificado en Base64.
     * Usamos columnDefinition="TEXT" para evitar el límite de VARCHAR en archivos grandes.
     */
    @Column(name = "content_base64", nullable = false, columnDefinition = "TEXT")
    private String fileContentBase64;

    @Size(max=120)
    private String mimeType;

    @Column(nullable=false)
    private long sizeBytes;

    // Datos del médico que cargó la historia clínica
    @Column(name = "doctor_name", nullable = false)
    private String doctorName;

    @Column(name = "doctor_document_number", nullable = false)
    private String doctorDocumentNumber;

    @Column(name = "doctor_specialty")
    private String doctorSpecialty;

    @Column(nullable=false)
    private OffsetDateTime uploadedAt = OffsetDateTime.now();
}
