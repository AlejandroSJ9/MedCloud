package com.medcloud.app.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name="doctors")
public class DoctorEntity extends BaseId{
    @OneToOne(optional=false)
    @JoinColumn(name="user_id", nullable=false, unique=true)
    private UserEntity user;

    @Size(max=60)
    private String specialty;

    @Size(max=40)
    private String licenseNumber;
}
