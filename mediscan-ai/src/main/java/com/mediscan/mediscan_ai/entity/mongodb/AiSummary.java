package com.mediscan.mediscan_ai.entity.mongodb;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "ai_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSummary {

    @Id
    private String id;

    @Field("report_id")
    private Long reportId;

    @Field("patient_id")
    private Long patientId;

    @Field("extracted_text")
    private String extractedText;

    @Field("summary")
    private String summary;

    @Field("key_findings")
    private List<String> keyFindings;

    @Field("abnormal_flags")
    private List<String> abnormalFlags;

    @Field("created_at")
    private LocalDateTime createdAt;
}