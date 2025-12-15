package com.swcampus.infra.ocr;

import com.swcampus.domain.ocr.OcrClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class OcrClientImpl implements OcrClient {

    private final RestTemplate ocrRestTemplate;

    public OcrClientImpl(@Qualifier("ocrRestTemplate") RestTemplate ocrRestTemplate) {
        this.ocrRestTemplate = ocrRestTemplate;
    }

    @Value("${ocr.server.url:http://localhost:8000}")
    private String ocrServerUrl;

    @Override
    public List<String> extractText(byte[] imageBytes, String fileName) {
        try {
            String url = ocrServerUrl + "/ocr/extract";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = 
                new HttpEntity<>(body, headers);

            OcrResponse response = ocrRestTemplate.postForObject(
                url, requestEntity, OcrResponse.class
            );

            if (response != null && response.lines() != null) {
                return response.lines();
            }
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("OCR 서버 호출 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
