package com.outfit.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.outfit.mapper.SystemConfigMapper;
import com.outfit.service.ImageGenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class ImageGenServiceImpl implements ImageGenService {
    @Autowired private SystemConfigMapper configMapper;

    @Override
    public String generateImage(String prompt) {
        String apiKey = configMapper.getValueByKey("image_gen_api_key");
        String apiUrl = configMapper.getValueByKey("image_gen_api_url");

        if (apiKey != null && !apiKey.isEmpty() && apiUrl != null && !apiUrl.isEmpty()) {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("prompt", prompt);
                params.put("n", 1);
                params.put("size", "1024x1024");
                String body = JSONUtil.toJsonStr(params);
                String resp = HttpUtil.createPost(apiUrl)
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .body(body)
                        .execute()
                        .body();
                JSONObject json = JSONUtil.parseObj(resp);
                // 适配常见文生图API返回格式
                if (json.containsKey("data")) {
                    Object data = json.get("data");
                    if (data instanceof JSONObject) {
                        return ((JSONObject) data).getStr("url");
                    } else if (data instanceof cn.hutool.json.JSONArray) {
                        return ((cn.hutool.json.JSONArray) data).getJSONObject(0).getStr("url");
                    }
                }
            } catch (Exception ignored) {}
        }
        // 返回占位图（无免费API时使用）
        return "/api/placeholder/outfit?seed=" + System.currentTimeMillis();
    }
}
