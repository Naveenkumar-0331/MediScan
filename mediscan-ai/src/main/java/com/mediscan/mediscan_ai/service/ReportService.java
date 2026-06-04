package com.mediscan.mediscan_ai.service;

import com.mediscan.mediscan_ai.dto.response.ReportResponse;
import com.mediscan.mediscan_ai.entity.mysql.Report;
import com.mediscan.mediscan_ai.repository.mysql.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final MinioService minioService;

    public ReportResponse uploadReport(
            MultipartFile file,
            Long patientId,
            String reportType,
            Long doctorId) throws IOException {
        String contentType = file.getContentType();

        if(contentType == null || !List.of("application/pdf","image/jpeg","image/png")
                .contains(contentType))
        {
            throw new RuntimeException(
                    "Invalid filetype. Only PDF, JPEG, PNG allowed."
            );
        }

        String s3Key = minioService.uploadFile(
                file.getOriginalFilename(),
                file.getInputStream(),
                file.getSize(),
                contentType
        );

        Report report=Report.builder()
                .patientId(patientId)
                .uploadedBy(doctorId)
                .fileName(file.getOriginalFilename())
                .s3Key(s3Key)
                .fileSizeBytes(file.getSize())
                .mimeType(contentType)
                .reportType(Report.ReportType.valueOf(reportType))
                .status(Report.ReportStatus.PENDING)
                .build();
        Report saved=reportRepository.save(report);

        return ReportResponse.builder()
                .reportId(saved.getId())
                .fileName(saved.getFileName())
                .patientId(saved.getPatientId())
                .uploadedBy(saved.getUploadedBy())
                .reportType(saved.getReportType().name())
                .status(saved.getStatus().name())
                .uploadedAt(saved.getCreatedAt())
                .message("Report uploaded successfully")
                .build();
    }
}
