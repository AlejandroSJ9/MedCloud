package com.medcloud.app.domain.service;

import com.medcloud.app.persistence.entity.UserEntity;
import com.medcloud.app.persistence.repositoryimp.UserRepositoryImp;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepositoryImp userRepositoryImp;

    public UserService(UserRepositoryImp userRepositoryImp) {
        this.userRepositoryImp = userRepositoryImp;
    }

    public List<UserEntity> getAll(){
        List<UserEntity> list = this.userRepositoryImp.findAll();
        System.out.println("in service from: repository" + list.toString());
        return this.userRepositoryImp.findAll();
    }

    public UserEntity save(UserEntity user){
        return this.userRepositoryImp.save(user);
    }


}
