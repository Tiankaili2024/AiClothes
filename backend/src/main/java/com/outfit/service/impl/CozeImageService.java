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

    @Value("${coze.api-url}")
    private String apiUrl;

    @Value("${coze.api-key}")
    private String apiKey;

    @Value("${coze.workflow-id}")
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
        params.put("user_prompt", prompt);
        params.put("photo", photoUrl != null && !photoUrl.isEmpty() ? photoUrl : "");

        Map<String, Object> body = new HashMap<>();
        body.put("workflow_id", workflowId);
        body.put("parameters", params);

        String json;
        try { json = objectMapper.writeValueAsString(body); }
        catch (Exception e) { throw new RuntimeException("Coze serialize failed", e); }

        log.info("Coze request - workflow={}, promptLen={}, hasPhoto={}", workflowId, prompt.length(), photoUrl != null && !photoUrl.isEmpty());
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        for (int i = 1; i <= 3; i++) {
            try {
                String resp = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class).getBody();
                log.info("Coze resp {}/3: {}", i, (resp != null && resp.length() > 200 ? resp.substring(0, 200) + "..." : resp));

                CozeWorkflowResponse r = objectMapper.readValue(resp, CozeWorkflowResponse.class);

                if (r.getCode() != null && r.getCode() == 0) {
                    String imageUrl = extractImageUrl(r);
                    log.info("Coze image: {}", imageUrl);
                    return imageUrl;
                }

                log.warn("Coze {}/3: code={} msg={}", i, r.getCode(), r.getMsg());
                if (r.getCode() != null && r.getCode() == 4024 && i < 3) { sleep(2000); continue; }
                throw new RuntimeException("Coze error: code=" + r.getCode() + " msg=" + r.getMsg());
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().startsWith("Coze error")) throw e;
                if (i == 3) throw new RuntimeException("Coze failed: " + e.getMessage(), e);
                log.warn("Coze retry {}/3: {}", i, e.getMessage());
                sleep(2000);
            } catch (Exception e) {
                if (i == 3) throw new RuntimeException("Coze failed: " + e.getMessage(), e);
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
        if (r.getData() == null) throw new RuntimeException("Coze data is null");

        try {
            Map<String, Object> data = objectMapper.readValue(r.getData(), Map.class);
            for (String k : new String[]{"data", "image_url", "url", "output", "image", "img_url", "result"}) {
                Object v = data.get(k);
                if (v != null && v.toString().startsWith("http")) return v.toString();
            }
            for (Object v : data.values()) {
                if (v instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nested = (Map<String, Object>) v;
                    for (String k : new String[]{"data", "image_url", "url", "output", "image"}) {
                        Object nv = nested.get(k);
                        if (nv != null && nv.toString().startsWith("http")) return nv.toString();
                    }
                }
            }
        } catch (Exception ignored) {}

        String raw = r.getData();
        int idx = raw.indexOf("http");
        if (idx >= 0) {
            int end = raw.indexOf("\"", idx);
            if (end < 0) end = raw.indexOf(",", idx);
            if (end < 0) end = raw.indexOf("}", idx);
            if (end < 0) end = raw.length();
            return raw.substring(idx, end).trim();
        }
        throw new RuntimeException("No image URL in: " + raw.substring(0, Math.min(200, raw.length())));
    }
}