package com.medcloud.app.web.controller;

import com.medcloud.app.domain.dto.LoginRequest;
import com.medcloud.app.domain.dto.LoginResponse;
import com.medcloud.app.domain.service.EpsService;
import com.medcloud.app.persistence.entity.EpsEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.medcloud.app.domain.dto.PatientResponse;
import com.medcloud.app.domain.service.PatientService;
import com.medcloud.app.persistence.entity.PatientEntity;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final EpsService epsService;
    private final com.medcloud.app.security.JwtUtil jwtUtil;
    private final PatientService patientService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getIdentifier(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_USER");

            logger.info("User role for email: {} is {}", userDetails.getUsername(), role);

            LoginResponse response = new LoginResponse(token, userDetails.getUsername(), role);

            return ResponseEntity.ok(Map.of("token", response.getToken(), "email", response.getEmail(), "role", response.getRole()));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No autorizado");
            error.put("message", "Credenciales invalidas: El correo o la contraseña proporcionados son incorrectos.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No autorizado");
            error.put("message", "Usuario no encontrado: No existe ninguna cuenta con el identificador proporcionado.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/patient-login")
    public ResponseEntity<?> patientLogin(@RequestParam String documentNumber) {
        Optional<PatientEntity> patientOpt = patientService.findByDocumentNumber(documentNumber);
        if (patientOpt.isPresent()) {
            PatientEntity patient = patientOpt.get();
            PatientResponse response = new PatientResponse(
                patient.getId(),
                patient.getDocumentNumber(),
                patient.getFullName(),
                patient.getBirthDate(),
                patient.getTreatment(),
                patient.isDiagnosisInProgress()
            );
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Paciente No Encontrado");
            error.put("message", "Ningún Paciente Encontrado Con Número de Documento.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

}