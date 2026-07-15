package com.outfit.dto;

import lombok.Data;

@Data
public class OutfitRequestDTO {
    private String userInput;
    private String templateType;
    private Integer customTemp;
    private Integer customWeather;
    private String customSeason;
    private String sessionId;
    private String refineType;
    private String city;
}
