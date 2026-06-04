package com.mediscan.mediscan_ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponse {
    private Long reportId;
    private String fileName;
    private Long patientId;
    private Long uploadedBy;
    private String reportType;
    private String status;
    private LocalDateTime uploadedAt;
    private String message;
}
