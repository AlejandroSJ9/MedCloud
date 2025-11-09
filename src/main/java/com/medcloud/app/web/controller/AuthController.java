package com.medcloud.app.web.controller;

import com.medcloud.app.domain.dto.LoginRequest;
import com.medcloud.app.domain.dto.LoginResponse;
import com.medcloud.app.domain.service.UserService;
import com.medcloud.app.persistence.entity.UserEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final com.medcloud.app.security.JwtUtil jwtUtil;

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

            LoginResponse response = new LoginResponse(token, userDetails.getUsername(), role);

            return ResponseEntity.ok(Map.of("token", response.getToken(), "email", response.getEmail(), "role", response.getRole()));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unauthorized");
            error.put("message", "Invalid credentials: The provided identifier or password is incorrect.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unauthorized");
            error.put("message", "User not found: No account exists with the provided identifier.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/patient-login")
    public ResponseEntity<Map<String, String>> patientLogin(@RequestParam String documentNumber) {
        try {
            // Find user by document number
            Optional<UserEntity> userOpt = userService.findByDocumentNumber(documentNumber);

            if (userOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Not Found");
                error.put("message", "Patient not found with the provided document number.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            UserEntity user = userOpt.get();

            // Check if user is a patient
            boolean isPatient = user.getRoles().stream()
                    .anyMatch(role -> "PACIENTE".equals(role.getName().name()));

            if (!isPatient) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Forbidden");
                error.put("message", "The provided document number does not belong to a patient.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Return patient info without JWT
            Map<String, String> response = new HashMap<>();
            response.put("documentNumber", user.getDocumentNumber());
            response.put("fullName", user.getFullName());
            response.put("role", "PACIENTE");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal Server Error");
            error.put("message", "An error occurred while processing the patient login.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}