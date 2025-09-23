package com.medcloud.app.persistence.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(name="patients",
        indexes = @Index(name="ix_patient_document", columnList="documentType,documentNumber", unique=true)
)
public class PatientEntity extends BaseId{
    @OneToOne(optional=false)
    @JoinColumn(name="user_id", nullable=false, unique=true)
    private UserEntity user;

    @NotBlank
    @Size(max=15)
    private String documentType;      // CC, CE, etc.

    @NotBlank @Size(max=30)
    private String documentNumber;

    @Size(max=120)
    private String fullName;

    private LocalDate birthDate;

}
