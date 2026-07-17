package com.outfit.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.outfit.dto.CozeWorkflowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CozeImageService {

    private static final Logger log = LoggerFactory.getLogger(CozeImageService.class);

    @Value("${coze.api-url}")
    private String apiUrl;

    @Value("${coze.api-key}")
    private String apiKey;

    @Value("${coze.workflow-id}")
    private String workflowId;

    @Value("${app.generated-dir:uploads/generated}")
    private String generatedDir;

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

        log.info("Coze request - workflow={}, promptLen={}", workflowId, prompt.length());
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        String cozeImageUrl = null;
        for (int i = 1; i <= 3; i++) {
            try {
                String resp = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class).getBody();
                CozeWorkflowResponse r = objectMapper.readValue(resp, CozeWorkflowResponse.class);

                if (r.getCode() != null && r.getCode() == 0) {
                    cozeImageUrl = extractImageUrl(r);
                    log.info("Coze image URL: {}", cozeImageUrl);
                    break;
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

        if (cozeImageUrl == null) throw new RuntimeException("Coze failed to generate image");

        // Download and save locally
        return saveImageLocally(cozeImageUrl);
    }

    private String saveImageLocally(String cozeUrl) {
        try {
            // Ensure directory exists
            String userDir = System.getProperty("user.dir");
            Path dirPath = Paths.get(userDir, generatedDir);
            Files.createDirectories(dirPath);

            // Generate unique filename
            String ext = "png";
            if (cozeUrl.contains(".")) {
                String query = cozeUrl.contains("?") ? cozeUrl.substring(0, cozeUrl.indexOf("?")) : cozeUrl;
                int dotIdx = query.lastIndexOf(".");
                if (dotIdx > 0) {
                    String possibleExt = query.substring(dotIdx + 1).toLowerCase();
                    if (possibleExt.matches("png|jpg|jpeg|gif|webp|bmp")) ext = possibleExt;
                }
            }
            String filename = UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() + "." + ext;
            Path targetPath = dirPath.resolve(filename);

            // Download the image
            log.info("Downloading Coze image from {} to {}", cozeUrl, targetPath);
            try (InputStream in = new URL(cozeUrl).openStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            long fileSize = Files.size(targetPath);
            log.info("Saved locally: {} ({} bytes)", targetPath, fileSize);

            // Return the accessible URL path
            String localUrl = "/uploads/generated/" + filename;
            log.info("Local image URL: {}", localUrl);
            return localUrl;
        } catch (IOException e) {
            log.error("Failed to save Coze image locally: {}", e.getMessage());
            // Fallback to original URL
            return cozeUrl;
        }
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