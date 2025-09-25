package com.medcloud.app.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "patients")
public class PatientEntity extends BaseId {

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;


    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

}