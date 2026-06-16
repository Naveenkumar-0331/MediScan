package com.mediscan.mediscan_ai.controller;

import com.mediscan.mediscan_ai.dto.response.ReportResponse;
import com.mediscan.mediscan_ai.entity.mongodb.AiSummary;
import com.mediscan.mediscan_ai.entity.mysql.Report;
import com.mediscan.mediscan_ai.entity.mysql.User;
import com.mediscan.mediscan_ai.repository.mongodb.AiSummaryRepository;
import com.mediscan.mediscan_ai.repository.mysql.ReportRepository;
import com.mediscan.mediscan_ai.repository.mysql.UserRepository;
import com.mediscan.mediscan_ai.service.AuditLogService;
import com.mediscan.mediscan_ai.service.ReportService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;


@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final ReportRepository reportRepository;
    private final AiSummaryRepository aiSummaryRepository;

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
                        new RuntimeException("Report not found"));

        // Find AI summary in MongoDB using mongoDocId
        AiSummary summary = aiSummaryRepository
                .findById(report.getMongoDocId())
                .orElseThrow(() ->
                        new RuntimeException("Summary not found"));

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

}
