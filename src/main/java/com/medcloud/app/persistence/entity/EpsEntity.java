package com.medcloud.app.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name="eps",
        indexes = {
                @Index(name="ix_eps_username", columnList="username", unique=true),
                @Index(name="ix_eps_email", columnList="email", unique=true)
        }
)
public class EpsEntity extends BaseId{

    @NotBlank
    @Size(max=60)
    @Column(nullable=false, unique=true, length=60)
    private String username;

    @NotBlank
    @Column(nullable=false)
    private String passwordHash;

    @Email
    @NotBlank
    @Size(max=120)
    @Column(nullable=false, unique=true, length=120)
    private String email;

    @Column(nullable=false)
    private boolean enabled = true;

    @NotBlank
    @Column(name = "eps_name", nullable = false)
    @Size(max=120)
    private String epsName;

    @NotBlank
    @Column(name = "nit", unique = true)
    private String nit;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(name="eps_roles",
            joinColumns = @JoinColumn(name="eps_id"),
            inverseJoinColumns = @JoinColumn(name="role_id"))
    private Set<RoleEntity> roles = new HashSet<>();
}