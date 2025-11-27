package com.medcloud.app.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "patients",
        indexes = @Index(name="ix_patient_document", columnList="document_number", unique=true))
public class PatientEntity extends BaseId {

    @NotBlank
    @Column(name = "document_number", unique = true)
    private String documentNumber;

    @Column(name = "full_name", nullable = true)
    @Size(max=120)
    private String fullName;

    @Column(name = "birth_date", nullable = true)
    private LocalDate birthDate;

    @Column(name = "treatment", columnDefinition = "TEXT")
    private String treatment;

    @Column(name = "diagnosis_in_progress", nullable = false)
    private boolean diagnosisInProgress = false;

}