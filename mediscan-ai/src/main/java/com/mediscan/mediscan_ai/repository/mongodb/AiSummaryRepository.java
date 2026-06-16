package com.mediscan.mediscan_ai.repository.mongodb;

import com.mediscan.mediscan_ai.entity.mongodb.AiSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AiSummaryRepository
        extends MongoRepository<AiSummary, String> {
    Optional<AiSummary> findByReportId(Long reportId);
}