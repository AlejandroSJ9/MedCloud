package com.medcloud.app.domain.service;

import com.medcloud.app.domain.dto.UserRequestCreate;
import com.medcloud.app.domain.dto.UserResponse;
import com.medcloud.app.domain.enums.RoleName;
import com.medcloud.app.domain.exceptions.UserAlreadyExistException;
import com.medcloud.app.persistence.entity.DoctorEntity;
import com.medcloud.app.persistence.entity.PatientEntity;
import com.medcloud.app.persistence.entity.UserEntity;
import com.medcloud.app.persistence.mapper.UserMapper;
import com.medcloud.app.persistence.repositoryimp.UserRepositoryImp;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepositoryImp userRepositoryImp;
    private final UserMapper userMapper;
    private final PasswordHasher passwordHasher;

    public UserService(UserRepositoryImp userRepositoryImp, UserMapper userMapper, PasswordHasher passwordHasher) {
        this.userRepositoryImp = userRepositoryImp;
        this.userMapper = userMapper;
        this.passwordHasher = passwordHasher;
    }

    public List<UserEntity> getAll(){
        List<UserEntity> list = this.userRepositoryImp.findAll();
        System.out.println("in service from: repository" + list.toString());
        return this.userRepositoryImp.findAll();
    }

    public UserResponse save(UserRequestCreate request){
        if(this.userRepositoryImp.existByEmail(request.email())){
            throw new UserAlreadyExistException("user already exist");
        }

        UserEntity userBase = userMapper.toEntity(request);
        userBase.setPasswordHash(passwordHasher.hash(request.password()));

        if (request.role() == RoleName.DOCTOR) {
            DoctorEntity doctor = new DoctorEntity();
            doctor.setSpecialty(request.specialty());
            doctor.setLicenseNumber(request.licenseNumber());
            doctor.setUser(userBase);
            userBase.setDoctorProfile(doctor);

        } else if (request.role() == RoleName.PACIENTE) {
            PatientEntity patient = new PatientEntity();
            patient.setUser(userBase);
            userBase.setPatientProfile(patient);
        }

        UserEntity toSave = this.userRepositoryImp.save(userBase);
        return this.userMapper.toResponse(toSave);

    }


}
