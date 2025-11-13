package com.medcloud.app.persistence.jpa;

import com.medcloud.app.persistence.entity.EpsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaUser extends JpaRepository<EpsEntity, UUID> {
    boolean existsByEmail(String email);
    Optional<EpsEntity> findByEmail(String email);
    Optional<EpsEntity> findByUsername(String username);
}
