package com.medcloud.app.persistence.entity;

import com.medcloud.app.domain.enums.RoleName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="roles", uniqueConstraints = @UniqueConstraint(columnNames="name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity extends BaseId {
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, unique=true, length=20)
    private RoleName name;

}
