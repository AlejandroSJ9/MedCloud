package com.medcloud.app.web.controller;

import com.medcloud.app.domain.dto.CaptchaResponseDTO;
import com.medcloud.app.domain.dto.EpsValidationResponseDTO;
import com.medcloud.app.domain.service.AdresValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/validation/eps")
@RequiredArgsConstructor
public class EpsValidator {

    private final AdresValidationService adresValidationService;

    /**
     * Initiates EPS validation by filling document type and number, then fetching the captcha.
     *
     * @param tipoDocumento the document type
     * @param numeroDocumento the document number
     * @return ResponseEntity containing CaptchaResponseDTO
     */
    @PostMapping("/initiate")
    public ResponseEntity<CaptchaResponseDTO> initiateValidation(
            @RequestParam String tipoDocumento,
            @RequestParam String numeroDocumento) {
        try {
            CaptchaResponseDTO response = adresValidationService.initiateValidation(tipoDocumento, numeroDocumento);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // In case of unexpected errors, return a generic error response
            CaptchaResponseDTO errorResponse = new CaptchaResponseDTO(null, null, "Internal server error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Validates EPS with the provided session ID, document type, document number, and captcha solution.
     *
     * @param sessionId the session ID from initiation
     * @param tipoDocumento the document type
     * @param numeroDocumento the document number
     * @param captchaSolution the captcha solution
     * @return ResponseEntity containing EpsValidationResponseDTO
     */
    @PostMapping("/validate")
    public ResponseEntity<EpsValidationResponseDTO> validateEps(
            @RequestParam String sessionId,
            @RequestParam String tipoDocumento,
            @RequestParam String numeroDocumento,
            @RequestParam String captchaSolution) {
        try {
            EpsValidationResponseDTO response = adresValidationService.validateEps(sessionId, captchaSolution, tipoDocumento, numeroDocumento);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // In case of unexpected errors, return a generic error response
            EpsValidationResponseDTO errorResponse = new EpsValidationResponseDTO(false, null, null, "Internal server error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}