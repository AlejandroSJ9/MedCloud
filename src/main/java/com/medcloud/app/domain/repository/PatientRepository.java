package com.medcloud.app.domain.repository;

import com.medcloud.app.persistence.entity.PatientEntity;

import java.util.UUID;

public interface PatientRepository extends BaseRepository<PatientEntity, UUID> {
}
