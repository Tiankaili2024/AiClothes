package com.outfit.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.outfit.dto.CozeWorkflowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CozeImageService {

    private static final Logger log = LoggerFactory.getLogger(CozeImageService.class);

    @Value("")
    private String apiUrl;

    @Value("")
    private String apiKey;

    @Value("")
    private String workflowId;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public CozeImageService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String generateOutfitImage(String prompt) {
        return generateOutfitImage(prompt, null);
    }

    public String generateOutfitImage(String prompt, String photoUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> params = new HashMap<>();
        params.put("Bizz", "generate");
        params.put("text", prompt);
        params.put("photo", photoUrl != null ? photoUrl : "");

        Map<String, Object> body = new HashMap<>();
        body.put("workflow_id", workflowId);
        body.put("parameters", params);

        String json;
        try { json = objectMapper.writeValueAsString(body); }
        catch (Exception e) { throw new RuntimeException("Coze serialize failed", e); }

        log.info("Coze call: {}", json);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        for (int i = 1; i <= 3; i++) {
            try {
                String resp = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class).getBody();
                CozeWorkflowResponse r = objectMapper.readValue(resp, CozeWorkflowResponse.class);
                if (r.getCode() != null && r.getCode() == 0) return extractImageUrl(r);
                log.warn("Coze attempt {}/3: code={} msg={}", i, r.getCode(), r.getMsg());
                if (r.getCode() != null && r.getCode() == 4024 && i < 3) { sleep(2000); continue; }
                throw new RuntimeException("Coze error: " + r.getMsg());
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().startsWith("Coze error")) throw new RuntimeException(e.getMessage(), e);
                if (i == 3) throw new RuntimeException("Coze failed: " + e.getMessage());
                log.warn("Coze retry {}/3: {}", i, e.getMessage());
                sleep(2000);
            }
        }
        throw new RuntimeException("Coze failed");
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private String extractImageUrl(CozeWorkflowResponse r) {
        try {
            Map<String, Object> data = objectMapper.readValue(r.getData(), Map.class);
            for (String k : new String[]{"image_url", "url", "output"}) {
                Object v = data.get(k);
                if (v != null) return v.toString();
            }
        } catch (Exception ignored) {}
        throw new RuntimeException("No image URL: " + r.getData());
    }
}
