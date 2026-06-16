package com.mediscan.mediscan_ai.controller;

import com.mediscan.mediscan_ai.dto.response.ReportResponse;
import com.mediscan.mediscan_ai.entity.mongodb.AiSummary;
import com.mediscan.mediscan_ai.entity.mysql.Report;
import com.mediscan.mediscan_ai.entity.mysql.User;
import com.mediscan.mediscan_ai.exception.ResourceNotFoundException;
import com.mediscan.mediscan_ai.repository.mongodb.AiSummaryRepository;
import com.mediscan.mediscan_ai.repository.mysql.ReportRepository;
import com.mediscan.mediscan_ai.repository.mysql.UserRepository;
import com.mediscan.mediscan_ai.service.AuditLogService;
import com.mediscan.mediscan_ai.service.ReportService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final ReportRepository reportRepository;
    private final AiSummaryRepository aiSummaryRepository;


    private final S3Client s3Client;

    @Value("${app.minio.bucket-reports}")
    private String bucketName;

    @PostMapping("/upload")
            public ResponseEntity<ReportResponse>uploadReport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("patientId")Long patientId,
            @RequestParam("reportType") String reportType,HttpServletRequest request) throws IOException{
        String email= SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        ReportResponse response=reportService.uploadReport(file,patientId,reportType,doctor.getId());

        try {
            auditLogService.log(
                    doctor.getId(),
                    doctor.getEmail(),
                    "REPORT_UPLOAD",
                    "REPORT",
                    response.getReportId().toString(),
                    request.getRemoteAddr(),
                    true,
                    "File: " + file.getOriginalFilename()
            );
        }
        catch(Exception e)
        {
            System.err.println("Audit log failed: "+e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}/summary")
    public ResponseEntity<?> getReportSummary(
            @PathVariable Long id,
            HttpServletRequest request) {

        // Get logged-in doctor
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found"));

        // Find report in MySQL
        Report report = reportRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Report not found"));

        // Find AI summary in MongoDB using mongoDocId
        AiSummary summary = aiSummaryRepository
                .findById(report.getMongoDocId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Summary not found"));

        // Log the access
        auditLogService.log(
                doctor.getId(),
                doctor.getEmail(),
                "REPORT_VIEW",
                "REPORT",
                id.toString(),
                request.getRemoteAddr(),
                true,
                "Viewed summary for report: " + id
        );

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/patients/{patientId}")
    public ResponseEntity<List<ReportResponse>> getPatientReports(
            @PathVariable Long patientId,
            HttpServletRequest request
    ){
        String email=SecurityContextHolder.getContext().getAuthentication().getName();

        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<Report> reports=reportRepository.findByPatientId(patientId);
        List<ReportResponse> responseList=reports.stream()
                .map(r->ReportResponse.builder()
                        .reportId(r.getId())
                        .fileName(r.getFileName())
                        .patientId(r.getPatientId())
                        .uploadedBy(r.getUploadedBy())
                        .reportType(r.getReportType().toString())
                        .status(r.getStatus().toString())
                        .uploadedAt(r.getUpdatedAt())
                        .message("Report retrieved")
                        .build()).toList();

        auditLogService.log(
                doctor.getId(),
                doctor.getEmail(),
                "PATIENT_REPORT_VIEW",
                "PATIENT",
                patientId.toString(),
                request.getRemoteAddr(),true,
                "Listed reports for patient: "+patientId
        );

        return ResponseEntity.ok(responseList) ;
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(
            @PathVariable Long id,
            HttpServletRequest request)
    {
        String email=SecurityContextHolder.getContext().getAuthentication().getName();

        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Find report in MySQL
        Report report = reportRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Report not found"));

        s3Client.deleteObject(b -> b.bucket(bucketName).key(report.getS3Key()));

        aiSummaryRepository.deleteById(report.getMongoDocId());
        reportRepository.deleteById(id);

        auditLogService.log(
                doctor.getId(),
                doctor.getEmail(),
                "REPORT_DELETE",
                "REPORT",
                id.toString(),
                request.getRemoteAddr(),true,
                "Deleted Report: "+id
        );

        return ResponseEntity.noContent().build();

    }
}
