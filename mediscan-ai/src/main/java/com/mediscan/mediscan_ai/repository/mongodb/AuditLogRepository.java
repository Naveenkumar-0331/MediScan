package com.mediscan.mediscan_ai.repository.mongodb;

import com.mediscan.mediscan_ai.entity.mongodb.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository
        extends MongoRepository<AuditLog, String> {

    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);
    List<AuditLog> findByResourceIdOrderByTimestampDesc(
            String resourceId);
    List<AuditLog> findByActionOrderByTimestampDesc(String action);
}