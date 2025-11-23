package com.medcloud.app.web.controller;

import com.medcloud.app.domain.dto.EpsCreateRequest;
import com.medcloud.app.domain.dto.UserResponse;
import com.medcloud.app.domain.service.EpsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/eps")
public class EpsController {

    private final EpsService epsService;

    public EpsController(EpsService epsService) {
        this.epsService = epsService;
    }

    @PostMapping("/")
    public ResponseEntity<?> createEps(@RequestBody EpsCreateRequest request) {
        try {
            UserResponse response = epsService.saveEps(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal Server Error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}