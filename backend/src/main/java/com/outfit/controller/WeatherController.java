package com.outfit.controller;

import com.outfit.common.Result;
import com.outfit.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    @Autowired private WeatherService weatherService;

    @GetMapping("/current")
    public Result<?> getCurrentWeather(@RequestParam(required = false) String city) {
        if (city != null && !city.isEmpty()) {
            return Result.success(weatherService.getWeather(city));
        }
        return Result.success(weatherService.getDefaultWeather());
    }
}
