package com.mediscan.mediscan_ai.repository.mysql;

import com.mediscan.mediscan_ai.entity.mysql.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report,Long> {
    List<Report> findByPatientId(Long patientId);
    List<Report> findByUploadedBy(Long doctorId);
    List<Report> findByStatus(Report.ReportStatus status);
}
