package com.medcloud.app.persistence.mapper;

import com.medcloud.app.domain.dto.UserRequestCreate;
import com.medcloud.app.domain.dto.UserResponse;
import com.medcloud.app.domain.enums.RoleName;
import com.medcloud.app.persistence.entity.EpsEntity;
import com.medcloud.app.persistence.entity.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EpsMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true) // se setea en el servicio
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "epsName", source = "fullName")
    @Mapping(target = "nit", ignore = true)
    EpsEntity toEntity(UserRequestCreate request);

    // EpsEntity → UserResponse
    UserResponse toResponse(EpsEntity eps);


    default Set<RoleName> mapRoles(Set<RoleEntity> roles) {
        return roles.stream().map(RoleEntity::getName).collect(Collectors.toSet());
    }
}
