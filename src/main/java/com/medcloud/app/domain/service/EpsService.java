package com.medcloud.app.domain.service;

import com.medcloud.app.domain.dto.EpsCreateRequest;
import com.medcloud.app.domain.dto.UserRequestCreate;
import com.medcloud.app.domain.dto.UserResponse;
import com.medcloud.app.domain.enums.RoleName;
import com.medcloud.app.domain.exceptions.UserAlreadyExistException;
import com.medcloud.app.persistence.entity.EpsEntity;
import com.medcloud.app.persistence.entity.RoleEntity;
import com.medcloud.app.persistence.jpa.JpaRole;
import com.medcloud.app.persistence.mapper.EpsMapper;
import com.medcloud.app.persistence.repositoryimp.UserRepositoryImp;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EpsService {

    private final UserRepositoryImp userRepositoryImp;
    private final EpsMapper userMapper;
    private final PasswordHasher passwordHasher;
    private final JpaRole jpaRole;

    public EpsService(UserRepositoryImp userRepositoryImp, EpsMapper userMapper, PasswordHasher passwordHasher, JpaRole jpaRole) {
        this.userRepositoryImp = userRepositoryImp;
        this.userMapper = userMapper;
        this.passwordHasher = passwordHasher;
        this.jpaRole = jpaRole;
    }

    public List<UserResponse> getAll(){
        List<EpsEntity> list = this.userRepositoryImp.findAll();
        System.out.println("in service from: repository" + list.toString());
        return list.stream().map(userMapper::toResponse).collect(Collectors.toList());
    }

    public UserResponse save(UserRequestCreate request){
        if(this.userRepositoryImp.existByEmail(request.email())){
            throw new UserAlreadyExistException("user already exist");
        }

        EpsEntity epsBase = userMapper.toEntity(request);
        epsBase.setPasswordHash(passwordHasher.hash(request.password()));

        // Assign EPS role for EPS users
        RoleEntity role = jpaRole.findByName(RoleName.EPS).orElse(new RoleEntity());
        role.setName(RoleName.EPS);
        epsBase.getRoles().add(role);

        EpsEntity toSave = this.userRepositoryImp.save(epsBase);
        return this.userMapper.toResponse(toSave);

    }

    public UserResponse saveEps(EpsCreateRequest request){
        if(this.userRepositoryImp.existByEmail(request.email())){
            throw new UserAlreadyExistException("user already exist");
        }

        EpsEntity epsEntity = userMapper.toEpsEntity(request);
        epsEntity.setPasswordHash(passwordHasher.hash(request.password()));

        // Assign EPS role
        Optional<RoleEntity> existingRole = jpaRole.findByName(RoleName.EPS);
        RoleEntity role;
        if (existingRole.isPresent()) {
            role = existingRole.get();
        } else {
            role = new RoleEntity();
            role.setName(RoleName.EPS);
            role = jpaRole.save(role);
        }
        epsEntity.getRoles().add(role);

        EpsEntity saved = this.userRepositoryImp.save(epsEntity);
        return this.userMapper.toResponse(saved);
    }

    public void updateRole(String email, RoleName newRole) {
        EpsEntity user = this.userRepositoryImp.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Clear existing roles and assign EPS role for EPS users
        user.getRoles().clear();
        RoleEntity role = jpaRole.findByName(RoleName.EPS).orElse(new RoleEntity());
        role.setName(RoleName.EPS);
        user.getRoles().add(role);

        this.userRepositoryImp.save(user);
    }

}
