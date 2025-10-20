package com.medcloud.app.persistence.mapper;

import com.medcloud.app.domain.dto.ClinicalDocumentCreateRequest;
import com.medcloud.app.domain.dto.ClinicalDocumentDto;
import com.medcloud.app.persistence.entity.ClinicalDocument;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Componente para mapear entre DTOs y Entidades de ClinicalDocument, ubicado en la capa de persistencia.
 */
@Component
public class ClinicalDocumentMapper {

    public ClinicalDocument toEntity(ClinicalDocumentCreateRequest dto) {
        if (dto == null) return null;

        ClinicalDocument entity = new ClinicalDocument();
        entity.setKind(dto.getKind());
        entity.setFilename(dto.getFilename());
        entity.setFileContentBase64(dto.getFileContentBase64());
        entity.setMimeType(dto.getMimeType());
        entity.setSizeBytes(dto.getSizeBytes());
        entity.setUploadedAt(OffsetDateTime.now());

        return entity;
    }

    public ClinicalDocumentDto toDto(ClinicalDocument entity) {
        if (entity == null) return null;

        return ClinicalDocumentDto.builder()
                .id(entity.getId() != null ? entity.getId().toString() : null)
                .patientId(entity.getPatient() != null && entity.getPatient().getId() != null ? entity.getPatient().getId().toString() : null)
                .uploadedByUserId(entity.getUploadedBy() != null && entity.getUploadedBy().getId() != null ? entity.getUploadedBy().getId().toString() : null)
                .kind(entity.getKind())
                .filename(entity.getFilename())
                .fileContentBase64(entity.getFileContentBase64())
                .mimeType(entity.getMimeType())
                .sizeBytes(entity.getSizeBytes())
                .uploadedAt(entity.getUploadedAt())
                .build();
    }
    // Dentro de ClinicalDocumentMapper.java

    public List<ClinicalDocumentDto> toDtoList(List<ClinicalDocument> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDto)
                .toList(); // Usar .collect(Collectors.toList()) si no estás en Java 16+
    }
}
