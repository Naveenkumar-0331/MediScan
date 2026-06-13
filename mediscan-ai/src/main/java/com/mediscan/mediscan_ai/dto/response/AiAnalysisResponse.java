package com.mediscan.mediscan_ai.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAnalysisResponse {
    private String mongoDocId;
    private String summary;
    private String status;
    private String extractedText;
}
