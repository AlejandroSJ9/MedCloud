package com.medcloud.app.persistence.jpa;

import com.medcloud.app.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaUser extends JpaRepository<UserEntity, UUID> {
}
