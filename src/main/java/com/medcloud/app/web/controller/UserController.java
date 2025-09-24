package com.medcloud.app.web.controller;

import com.medcloud.app.domain.service.UserService;
import com.medcloud.app.persistence.entity.UserEntity;
import org.apache.catalina.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public ResponseEntity<List<UserEntity>> getAll(){
        List<UserEntity> list = this.userService.getAll();
        System.out.println("in controller from service: " + list.toString());
        return ResponseEntity.ok(userService.getAll());
    }

    @PostMapping("/")
    public ResponseEntity<UserEntity> save(@RequestBody UserEntity user){
        return ResponseEntity.status(HttpStatus.CONTINUE).body(this.userService.save(user));
    }
}
