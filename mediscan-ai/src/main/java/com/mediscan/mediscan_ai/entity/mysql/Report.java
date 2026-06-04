package com.mediscan.mediscan_ai.entity.mysql;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long patientId;

    @Column
    private Long uploadedBy;

    @Column
    private  String fileName;

    @Column(name = "s3_key", length = 500)
    private String s3Key;

    @Column
    private Long fileSizeBytes;

    @Column(length=100)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @Column(length = 100)
    private String mongoDocId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum ReportType{
        LAB, XRAY , MRI, PRESCRIPTION , OTHER
    }

    public enum ReportStatus{
        PENDING, PROCESSING, DONE , FAILED
    }


}
