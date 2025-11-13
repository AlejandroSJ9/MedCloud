package com.medcloud.app.domain.repository;

import com.medcloud.app.persistence.entity.EpsEntity;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends BaseRepository<EpsEntity, UUID> {
    boolean existByEmail(String email);
}
