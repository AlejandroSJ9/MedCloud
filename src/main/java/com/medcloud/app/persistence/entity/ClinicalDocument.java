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
        indexes = @Index(name="ix_doc_patient", columnList="patient_id"))
public class ClinicalDocument extends BaseId{
    @ManyToOne(optional=false) @JoinColumn(name="patient_id", nullable=false)
    private PatientEntity patient;

    // Quién subió el documento
    @ManyToOne(optional=false) @JoinColumn(name="uploaded_by_user_id", nullable=false)
    private UserEntity uploadedBy;

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

    @Column(nullable=false)
    private OffsetDateTime uploadedAt = OffsetDateTime.now();
}
