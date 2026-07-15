package com.outfit.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.outfit.mapper.SystemConfigMapper;
import com.outfit.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeatherServiceImpl implements WeatherService {
    @Autowired private SystemConfigMapper configMapper;

    @Override
    public Map<String, Object> getWeather(String city) {
        String apiKey = configMapper.getValueByKey("weather_api_key");
        String apiUrl = configMapper.getValueByKey("weather_api_url");
        if (apiKey == null || apiKey.isEmpty()) {
            return getDefaultWeather();
        }
        try {
            String resp = HttpUtil.get(String.format("%s?key=%s&city=%s&extensions=base", apiUrl, apiKey, city));
            JSONObject json = JSONUtil.parseObj(resp);
            if ("1".equals(json.getStr("status"))) {
                JSONObject live = json.getJSONArray("lives").getJSONObject(0);
                Map<String, Object> result = new HashMap<>();
                result.put("city", live.getStr("city"));
                result.put("temperature", live.getStr("temperature"));
                result.put("weather", live.getStr("weather"));
                result.put("humidity", live.getStr("humidity"));
                result.put("windpower", live.getStr("windpower"));
                result.put("season", getSeason(Integer.parseInt(live.getStr("temperature"))));
                return result;
            }
        } catch (Exception ignored) {}
        return getDefaultWeather();
    }

    @Override
    public Map<String, Object> getDefaultWeather() {
        Map<String, Object> result = new HashMap<>();
        result.put("city", "默认城市");
        result.put("temperature", "25");
        result.put("weather", "晴");
        result.put("season", "春季");
        return result;
    }

    private String getSeason(int temp) {
        if (temp >= 28) return "夏季";
        if (temp >= 15) return "春季";
        if (temp >= 5) return "秋季";
        return "冬季";
    }
}
