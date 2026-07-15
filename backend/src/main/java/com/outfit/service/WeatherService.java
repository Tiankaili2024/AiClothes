package com.outfit.service;

import java.util.Map;

public interface WeatherService {
    Map<String, Object> getWeather(String city);
    Map<String, Object> getDefaultWeather();
}
