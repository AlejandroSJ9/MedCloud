package com.medcloud.app.domain.repository;

import java.util.List;
import java.util.Optional;

public interface BaseRepository <T, ID>{
    T save(T toSave);
    Optional<T> findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
    boolean existsById(ID id);
}
