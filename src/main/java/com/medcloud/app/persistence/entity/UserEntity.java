package com.medcloud.app.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="users",
        indexes = {
                @Index(name="ix_user_username", columnList="username", unique=true),
                @Index(name="ix_user_email", columnList="email", unique=true)
        }
)
public class UserEntity extends BaseId{
    @NotBlank
    @Size(max=60)
    @Column(nullable=false, unique=true, length=60)
    private String username;

    @NotBlank
    @Column(nullable=false)
    private String passwordHash;

    @Email
    @NotBlank @Size(max=120)
    @Column(nullable=false, unique=true, length=120)
    private String email;

    @Column(nullable=false)
    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="user_roles",
            joinColumns = @JoinColumn(name="user_id"),
            inverseJoinColumns = @JoinColumn(name="role_id"))
    private Set<RoleEntity> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private DoctorEntity doctorProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private PatientEntity patientProfile;
}
