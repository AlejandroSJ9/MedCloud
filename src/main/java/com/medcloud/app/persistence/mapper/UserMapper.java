package com.medcloud.app.persistence.mapper;

import com.medcloud.app.domain.dto.UserRequestCreate;
import com.medcloud.app.domain.dto.UserResponse;
import com.medcloud.app.domain.enums.RoleName;
import com.medcloud.app.persistence.entity.RoleEntity;
import com.medcloud.app.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true) // se setea en el servicio
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "doctorProfile", ignore = true)
    @Mapping(target = "patientProfile", ignore = true)
    UserEntity toEntity(UserRequestCreate request);

    // UserEntity → UserResponse
    UserResponse toResponse(UserEntity user);


    default Set<RoleName> mapRoles(Set<RoleEntity> roles) {
        return roles.stream().map(RoleEntity::getName).collect(Collectors.toSet());
    }
}
