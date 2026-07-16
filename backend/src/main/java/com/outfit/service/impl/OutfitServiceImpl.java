package com.outfit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.outfit.dto.OutfitRequestDTO;
import com.outfit.entity.OutfitRecord;
import com.outfit.entity.User;
import com.outfit.mapper.OutfitRecordMapper;
import com.outfit.mapper.UserMapper;
import com.outfit.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OutfitServiceImpl implements OutfitService {
    @Autowired private OutfitRecordMapper recordMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private AIService aiService;
    @Autowired private ImageGenService imageGenService;
    @Autowired private WeatherService weatherService;

    private static final String DEFAULT_CITY = "北京";

    private final Map<String, Integer> dailyLimitMap = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public OutfitRecord generateOutfit(Long userId, OutfitRequestDTO dto) {
        String limitKey = "daily:" + userId + ":" + LocalDate.now();
        Integer count = dailyLimitMap.get(limitKey);
        if (count != null && count >= 100) {
            throw new RuntimeException("今日生成次数已达上限(100次)");
        }

        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");

        String city = (dto.getCity() != null && !dto.getCity().isEmpty())
                ? dto.getCity()
                : (user.getCity() != null && !user.getCity().isEmpty()
                    ? user.getCity()
                    : DEFAULT_CITY);

        Map<String, Object> weather = weatherService.getWeather(city);
        String weatherInfo = (String) weather.get("weather");
        String temperature = (String) weather.get("temperature");
        String season = (String) weather.getOrDefault("season", "春季");
        String regionType = (String) weather.getOrDefault("regionType", "北方");

        String jsonResult = aiService.generateOutfitPlan(user, dto.getUserInput(), weatherInfo, temperature, city, season, regionType, null);

        String sessionId = java.util.UUID.randomUUID().toString().substring(0,8);
        OutfitRecord firstRecord = null;
        for (int i = 0; i < 3; i++) {
            OutfitRecord record = new OutfitRecord();
            record.setUserId(userId);
            record.setUserInput(dto.getUserInput());
            record.setWeatherInfo(weather.toString());
            record.setParsedParams(jsonResult);
            record.setSessionId(sessionId);
            record.setSchemeIndex(i + 1);
            record.setStatus(1);
            recordMapper.insert(record);
            if (i == 0) firstRecord = record;
        }

        dailyLimitMap.put(limitKey, dailyLimitMap.getOrDefault(limitKey, 0) + 1);
        return firstRecord;
    }

    @Override
    public OutfitRecord getRecordDetail(Long recordId, Long userId) {
        OutfitRecord record = recordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new RuntimeException("记录不存在");
        }
        return record;
    }

    @Override
    public Page<OutfitRecord> getUserRecords(Long userId, int page, int size) {
        LambdaQueryWrapper<OutfitRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutfitRecord::getUserId, userId);
        wrapper.orderByDesc(OutfitRecord::getCreatedAt);
        return recordMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public void deleteRecord(Long recordId, Long userId) {
        OutfitRecord record = recordMapper.selectById(recordId);
        if (record != null && record.getUserId().equals(userId)) {
            recordMapper.deleteById(recordId);
        }
    }

    @Override
    public void clearRecords(Long userId) {
        LambdaQueryWrapper<OutfitRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutfitRecord::getUserId, userId);
        recordMapper.delete(wrapper);
    }

    @Override
    public Long getTodayCount(Long userId) {
        LambdaQueryWrapper<OutfitRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutfitRecord::getUserId, userId)
               .ge(OutfitRecord::getCreatedAt, LocalDateTime.of(LocalDate.now(), LocalTime.MIN))
               .le(OutfitRecord::getCreatedAt, LocalDateTime.of(LocalDate.now(), LocalTime.MAX));
        return recordMapper.selectCount(wrapper);
    }

    @Override
    public Object quickRefine(Long userId, com.outfit.dto.QuickRefineDTO dto) {
        return "优化建议功能开发中";
    }

    @Override
    public Object getSessionRecords(Long userId, String sessionId) {
        LambdaQueryWrapper<OutfitRecord> w = new LambdaQueryWrapper<>();
        w.eq(OutfitRecord::getSessionId, sessionId).eq(OutfitRecord::getUserId, userId);
        return recordMapper.selectList(w);
    }

    @Override
    public Map<String,Object> getUserPortrait(Long userId) {
        Map<String,Object> result = new HashMap<>();
        List<Map<String,Object>> styleList = recordMapper.userStyleStats(userId);
        Map<String,Object> scoreMap = recordMapper.userScoreStats(userId);
        result.put("styleDistribution", styleList != null ? styleList : new ArrayList<>());
        result.put("totalCount", scoreMap != null ? scoreMap.get("cnt") : 0);
        result.put("totalSchemes", 3);
        result.put("avgScore", 85);
        result.put("favCount", 0);
        List<Map<String,String>> templates = new ArrayList<>();
        Map<String,String> t1 = new HashMap<>(); t1.put("style", "通勤简约"); t1.put("reason", "根据你的穿搭偏好推荐的经典搭配"); templates.add(t1);
        Map<String,String> t2 = new HashMap<>(); t2.put("style", "休闲舒适"); t2.put("reason", "适合日常出行的百搭组合"); templates.add(t2);
        result.put("recommendedTemplates", templates);
        return result;
    }
}