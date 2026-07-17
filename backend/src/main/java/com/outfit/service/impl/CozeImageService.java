package com.outfit.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.outfit.dto.CozeWorkflowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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

        // Upload local photo to Coze file API, then pass file_id to workflow
        Object photoParam = "";
        if (photoUrl != null && !photoUrl.isEmpty()) {
            try {
                String resolvedPath = photoUrl;
                if (photoUrl.startsWith("http://localhost:8080")) {
                    String userDir = System.getProperty("user.dir");
                    resolvedPath = userDir + photoUrl.substring("http://localhost:8080".length());
                } else if (photoUrl.startsWith("/")) {
                    String userDir = System.getProperty("user.dir");
                    resolvedPath = userDir + photoUrl;
                }

                String fileId = uploadFileToCoze(resolvedPath);
                if (fileId != null) {
                    // Coze image parameter expects format: {"file_id": "xxx"}
                    Map<String, String> photoObj = new HashMap<>();
                    photoObj.put("file_id", fileId);
                    photoParam = photoObj;
                    log.info("Coze file_id for photo: {}", fileId);
                } else {
                    log.warn("Failed to upload photo to Coze, sending empty photo");
                }
            } catch (Exception e) {
                log.warn("Failed to upload photo to Coze: {}", e.getMessage());
            }
        }

        params.put("user_prompt", prompt);
        params.put("photo", photoParam);

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

        return saveImageLocally(cozeImageUrl);
    }

    private String saveImageLocally(String cozeUrl) {
        try {
            String userDir = System.getProperty("user.dir");
            Path dirPath = Paths.get(userDir, generatedDir);
            Files.createDirectories(dirPath);

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

            log.info("Downloading Coze image from {} to {}", cozeUrl, targetPath);
            try (InputStream in = new URL(cozeUrl).openStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            long fileSize = Files.size(targetPath);
            log.info("Saved locally: {} ({} bytes)", targetPath, fileSize);

            String localUrl = "/uploads/generated/" + filename;
            log.info("Local image URL: {}", localUrl);
            return localUrl;
        } catch (IOException e) {
            log.error("Failed to save Coze image locally: {}", e.getMessage());
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

    private String uploadFileToCoze(String filePath) {
        String uploadUrl = "https://api.coze.cn/v1/files/upload";
        String boundary = "----FormBoundary" + UUID.randomUUID().toString().replace("-", "");

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.warn("Photo file not found: {}", filePath);
                return null;
            }

            byte[] fileBytes = Files.readAllBytes(path);
            String fileName = path.getFileName().toString();
            String contentType = Files.probeContentType(path);
            if (contentType == null) contentType = "image/jpeg";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String header = "--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n"
                    + "Content-Type: " + contentType + "\r\n\r\n";
            baos.write(header.getBytes("UTF-8"));
            baos.write(fileBytes);
            baos.write(("\r\n--" + boundary + "\r\n").getBytes("UTF-8"));
            String purposePart = "Content-Disposition: form-data; name=\"purpose\"\r\n\r\nworkflow\r\n";
            baos.write(purposePart.getBytes("UTF-8"));
            baos.write(("--" + boundary + "--\r\n").getBytes("UTF-8"));

            byte[] bodyBytes = baos.toByteArray();

            HttpURLConnection conn = (HttpURLConnection) new URL(uploadUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setDoOutput(true);
            conn.getOutputStream().write(bodyBytes);

            int responseCode = conn.getResponseCode();
            InputStream responseStream = (responseCode >= 200 && responseCode < 300)
                    ? conn.getInputStream() : conn.getErrorStream();
            String responseBody = new String(responseStream.readAllBytes(), "UTF-8");

            log.info("Coze file upload response code={} body={}", responseCode, responseBody);

            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
            if (response.get("code") != null && (Integer) response.get("code") == 0) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                return (String) data.get("id");
            }
            log.warn("Coze upload failed: {}", responseBody);
            return null;
        } catch (Exception e) {
            log.warn("Coze upload exception: {}", e.getMessage());
            return null;
        }
    }
}