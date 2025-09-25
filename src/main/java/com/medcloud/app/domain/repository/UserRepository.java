package com.medcloud.app.domain.repository;

import com.medcloud.app.persistence.entity.UserEntity;

import java.util.UUID;

public interface UserRepository extends BaseRepository<UserEntity, UUID> {
    boolean existByEmail(String email);
}
