package com.mediscan.mediscan_ai.service;

import com.mediscan.mediscan_ai.entity.mongodb.AuditLog;
import com.mediscan.mediscan_ai.repository.mongodb.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(Long userId,
                    String userEmail,
                    String action,
                    String resourceType,
                    String resourceId,
                    String ipAddress,
                    boolean success,
                    String details) {

        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .userEmail(userEmail)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .ipAddress(ipAddress)
                .success(success)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
    }
}