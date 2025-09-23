package com.medcloud.app.persistence.entity;


import com.medcloud.app.domain.enums.DocumentKind;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

@Entity
@Table(name="clinical_documents",
        indexes = @Index(name="ix_doc_patient", columnList="patient_id"))
public class ClinicalDocument extends BaseId{
    @ManyToOne(optional=false) @JoinColumn(name="patient_id", nullable=false)
    private PatientEntity patient;

    // Quién subió el documento (útil para trazabilidad básica)
    @ManyToOne(optional=false) @JoinColumn(name="uploaded_by_user_id", nullable=false)
    private UserEntity uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=10)
    private DocumentKind kind = DocumentKind.PDF;

    @NotBlank
    @Size(max=200)
    private String filename;          // nombre visible

    @NotBlank @Size(max=400)
    private String storageUrl;        // ubicación en S3/Blob/etc.

    @Size(max=120)
    private String mimeType;          // "application/pdf"

    @Column(nullable=false)
    private long sizeBytes;

    @Column(nullable=false)
    private OffsetDateTime uploadedAt = OffsetDateTime.now();
}
