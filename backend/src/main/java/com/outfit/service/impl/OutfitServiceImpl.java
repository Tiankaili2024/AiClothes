package com.outfit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.outfit.dto.OutfitRequestDTO;
import com.outfit.entity.OutfitRecord;
import com.outfit.entity.User;
import com.outfit.mapper.OutfitRecordMapper;
import com.outfit.mapper.UserMapper;
import com.outfit.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OutfitServiceImpl implements OutfitService {
    private static final Logger log = LoggerFactory.getLogger(OutfitServiceImpl.class);
    private static final String DEFAULT_CITY = "\u5317\u4eac";
    private final Map<String, Integer> dailyLimit = new ConcurrentHashMap<>();

    @Autowired private OutfitRecordMapper recordMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private AIService aiService;
    @Autowired private ImageGenService imageGenService;
    @Autowired private CozeImageService cozeImageService;
    @Autowired private WeatherService weatherService;

    @Override
    @Transactional
    public OutfitRecord generateOutfit(Long userId, OutfitRequestDTO dto) {
        String limitKey = "daily:" + userId + ":" + LocalDate.now();
        if (dailyLimit.getOrDefault(limitKey, 0) >= 100)
            throw new RuntimeException("\u4eca\u65e5\u751f\u6210\u6b21\u6570\u5df2\u8fbe\u4e0a\u9650(100\u6b21)");

        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("\u7528\u6237\u4e0d\u5b58\u5728");

        String city = dto.getCity() != null && !dto.getCity().isEmpty() ? dto.getCity()
                : (user.getCity() != null && !user.getCity().isEmpty() ? user.getCity() : DEFAULT_CITY);

        Map<String, Object> weather = weatherService.getWeather(city);
        String jsonResult = aiService.generateOutfitPlan(user, dto.getUserInput(),
                (String) weather.get("weather"), (String) weather.get("temperature"),
                city, (String) weather.getOrDefault("season", "\u6625\u5b63"),
                (String) weather.getOrDefault("regionType", "\u5317\u65b9"), null);

        String photoUrl = (user.getFullBodyPhoto() != null && !user.getFullBodyPhoto().isEmpty())
                ? "http://localhost:8080" + user.getFullBodyPhoto() : null;

        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        OutfitRecord firstRecord = null;

        for (int i = 0; i < 3; i++) {
            OutfitRecord r = new OutfitRecord();
            r.setUserId(userId);
            r.setUserInput(dto.getUserInput());
            r.setWeatherInfo(weather.toString());
            r.setParsedParams(jsonResult);
            r.setSessionId(sessionId);
            r.setSchemeIndex(i + 1);
            r.setStatus(1);

            if (i == 0) {
                String prompt = buildPrompt(user, jsonResult, 0);
                r.setPrompt(prompt);
                try {
                    r.setImageUrl(cozeImageService.generateOutfitImage(prompt, photoUrl));
                    log.info("Coze image: {}", r.getImageUrl());
                } catch (Exception e) {
                    log.warn("Coze failed, fallback: {}", e.getMessage());
                    r.setImageUrl(imageGenService.generateImage(prompt));
                }
            }
            recordMapper.insert(r);
            if (i == 0) firstRecord = r;
        }
        dailyLimit.merge(limitKey, 1, Integer::sum);
        return firstRecord;
    }

    private String buildPrompt(User user, String json, int idx) {
        try {
            List<Map<String, Object>> plans = new ObjectMapper().readValue(json, List.class);
            if (plans != null && idx < plans.size()) {
                Map<String, Object> p = plans.get(idx);
                                String gender = (user.getGender() != null && user.getGender().contains("女")) ? "female" : "male";
                StringBuilder sb = new StringBuilder("A " + gender + " model wearing ");
                if (p.get("top") != null) sb.append(p.get("top")).append(", ");
                if (p.get("bottom") != null) sb.append(p.get("bottom")).append(", ");
                if (p.get("shoes") != null) sb.append(p.get("shoes")).append(", ");
                if (p.get("accessory") != null) sb.append("with ").append(p.get("accessory")).append(", ");
                if (p.get("style") != null) sb.append("Style: ").append(p.get("style"));
                sb.append(". Full body, high quality fashion photography.");
                return sb.toString();
            }
        } catch (Exception e) { log.warn("Prompt build failed: {}", e.getMessage()); }
        return "A male model wearing stylish clothes, full body shot, high quality.";
    }

    @Override public OutfitRecord getRecordDetail(Long recordId, Long userId) {
        OutfitRecord r = recordMapper.selectById(recordId);
        if (r == null || !r.getUserId().equals(userId)) throw new RuntimeException("\u8bb0\u5f55\u4e0d\u5b58\u5728");
        return r;
    }

    @Override public Page<OutfitRecord> getUserRecords(Long userId, int page, int size) {
        LambdaQueryWrapper<OutfitRecord> w = new LambdaQueryWrapper<>();
        w.eq(OutfitRecord::getUserId, userId).orderByDesc(OutfitRecord::getCreatedAt);
        return recordMapper.selectPage(new Page<>(page, size), w);
    }

    @Override public void deleteRecord(Long recordId, Long userId) {
        OutfitRecord r = recordMapper.selectById(recordId);
        if (r != null && r.getUserId().equals(userId)) recordMapper.deleteById(recordId);
    }

    @Override public void clearRecords(Long userId) {
        recordMapper.delete(new LambdaQueryWrapper<OutfitRecord>().eq(OutfitRecord::getUserId, userId));
    }

    @Override public Long getTodayCount(Long userId) {
        return recordMapper.selectCount(new LambdaQueryWrapper<OutfitRecord>()
                .eq(OutfitRecord::getUserId, userId)
                .ge(OutfitRecord::getCreatedAt, LocalDateTime.of(LocalDate.now(), LocalTime.MIN))
                .le(OutfitRecord::getCreatedAt, LocalDateTime.of(LocalDate.now(), LocalTime.MAX)));
    }

    @Override public Object quickRefine(Long userId, com.outfit.dto.QuickRefineDTO dto) {
        return "\u4f18\u5316\u5efa\u8bae\u529f\u80fd\u5f00\u53d1\u4e2d";
    }

    @Override public Object getSessionRecords(Long userId, String sessionId) {
        return recordMapper.selectList(new LambdaQueryWrapper<OutfitRecord>()
                .eq(OutfitRecord::getSessionId, sessionId).eq(OutfitRecord::getUserId, userId));
    }

    @Override public Map<String, Object> getUserPortrait(Long userId) {
        Map<String, Object> r = new HashMap<>();
        r.put("styleDistribution", recordMapper.userStyleStats(userId) != null ? recordMapper.userStyleStats(userId) : new ArrayList<>());
        r.put("totalCount", 0); r.put("totalSchemes", 3); r.put("avgScore", 85); r.put("favCount", 0);
        r.put("recommendedTemplates", Arrays.asList(
                Map.of("style", "\u901a\u52e4\u7b80\u7ea6", "reason", "\u7ecf\u5178\u642d\u914d"),
                Map.of("style", "\u4f11\u95f2\u8212\u9002", "reason", "\u65e5\u5e38\u642d\u914d")));
        return r;
    }
}
