package com.mediscan.mediscan_ai.service;

import com.mediscan.mediscan_ai.dto.response.AiAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiServiceClient {


    @Value("${app.ai-service.url}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate;

    public AiAnalysisResponse analyzeReport(
            MultipartFile file,
            Long reportId,
            Long patientId) throws IOException {

        // Build multipart form data
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // Add file as a resource
        ByteArrayResource fileResource = new ByteArrayResource(
                file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        body.add("file", fileResource);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        // Call Python service
        String url = aiServiceUrl + "/analyze?report_id="
                + reportId + "&patient_id=" + patientId;

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        // Map response to DTO
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            throw new RuntimeException("Empty response from AI service");
        }

        return AiAnalysisResponse.builder()
                .mongoDocId((String) responseBody.get("mongo_doc_id"))
                .summary((String) responseBody.get("summary"))
                .status((String) responseBody.get("status"))
                .extractedText((String) responseBody.get(
                        "extracted_text"))
                .build();
    }
}