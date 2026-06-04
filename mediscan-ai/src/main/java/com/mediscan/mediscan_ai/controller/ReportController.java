package com.mediscan.mediscan_ai.controller;

import com.mediscan.mediscan_ai.dto.response.ReportResponse;
import com.mediscan.mediscan_ai.entity.mysql.User;
import com.mediscan.mediscan_ai.repository.mysql.UserRepository;
import com.mediscan.mediscan_ai.service.ReportService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    @PostMapping("/upload")
            public ResponseEntity<ReportResponse>uploadReport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("patientId")Long patientId,
            @RequestParam("reportType") String reportType) throws IOException{
        String email= SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return ResponseEntity.ok(
                reportService.uploadReport(file, patientId, reportType, doctor.getId())
        );
    }

}
