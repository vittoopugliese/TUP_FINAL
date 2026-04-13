package com.inspections.service;

import com.inspections.entity.AuditLog;
import com.inspections.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro centralizado de auditoría. Los fallos al persistir no deben afectar la operación principal.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * @param userId email del actor (coincide con el principal JWT y createdByEmail en el proyecto)
     * @param metadataJson JSON opcional; puede ser null
     */
    public void log(String userId, String entityType, String entityId, String action, String metadataJson) {
        try {
            AuditLog row = new AuditLog();
            row.setId(UUID.randomUUID().toString());
            row.setUserId(userId != null ? userId : "");
            row.setEntityType(entityType != null ? entityType : "");
            row.setEntityId(entityId != null ? entityId : "");
            row.setAction(action != null ? action : "");
            row.setMetadataJson(metadataJson);
            row.setCreatedAt(Instant.now());
            auditLogRepository.save(row);
        } catch (Exception e) {
            log.warn("Audit log failed (non-fatal): action={}, entityType={}, entityId={}", action, entityType, entityId, e);
        }
    }
}
