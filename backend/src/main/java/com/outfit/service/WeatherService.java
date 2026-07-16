package com.outfit.service;

import java.util.Map;

public interface WeatherService {
    Map<String, Object> getWeather(String city);
    Map<String, Object> getDefaultWeather();
    boolean isNorthCity(String city);
    String getSeason(int temp);
}