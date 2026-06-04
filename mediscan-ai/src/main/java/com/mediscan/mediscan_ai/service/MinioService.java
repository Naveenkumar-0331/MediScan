package com.mediscan.mediscan_ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final S3Client s3Client;

    @Value("${app.minio.bucket-reports}")
            private String bucketName;
    @Value("${app.minio.endpoint}")
    private String endpoint;

    public String uploadFile(String fileName,
                             InputStream inputStream,
                             long fileSize,
                             String contentType)
    {
        String s3Key= UUID.randomUUID()+"_"+fileName;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(contentType)
                .contentLength(fileSize)
                .build();

        s3Client.putObject(request,
                RequestBody.fromInputStream(inputStream,fileSize));

        return s3Key;
    }

    public String generatePresignedUrl(String s3Key){
        S3Presigner presigner=S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(s3Client.serviceClientConfiguration()
                        .credentialsProvider())
                .region(Region.US_EAST_1)
                .build();

        GetObjectPresignRequest presignRequest=GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(r -> r.bucket(bucketName).key(s3Key))
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }
}
