package com.medcloud.app.persistence.repositoryimp;

import com.medcloud.app.domain.exceptions.UserAlreadyExistException;
import com.medcloud.app.domain.repository.UserRepository;
import com.medcloud.app.persistence.entity.EpsEntity;
import com.medcloud.app.persistence.jpa.JpaUser;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryImp implements UserRepository {

    private final JpaUser jpaUser;

    public UserRepositoryImp(JpaUser jpaUser) {
        this.jpaUser = jpaUser;
    }

    @Override
    public EpsEntity save(EpsEntity toSave) {
        if (this.jpaUser.existsByEmail(toSave.getEmail())){
            throw new UserAlreadyExistException("User already exist");
        }

        return this.jpaUser.save(toSave);
    }

    @Override
    public Optional<EpsEntity> findById(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public List<EpsEntity> findAll() {
        return this.jpaUser.findAll();
    }

    @Override
    public void deleteById(UUID uuid) {

    }

    @Override
    public boolean existsById(UUID uuid) {
        return false;
    }

    @Override
    public boolean existByEmail(String email) {
        return this.jpaUser.existsByEmail(email);
    }

    public Optional<EpsEntity> findByEmail(String email) {
        return this.jpaUser.findByEmail(email);
    }

    public Optional<EpsEntity> findByUsername(String username) {
        return this.jpaUser.findByUsername(username);
    }

}

