package com.mediscan.mediscan_ai.entity.mongodb;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "audit_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    private String id;

    private Long userId;
    private String userEmail;
    private String action;
    private String resourceType;
    private String resourceId;
    private String ipAddress;
    private boolean success;
    private String details;
    private LocalDateTime timestamp;
}