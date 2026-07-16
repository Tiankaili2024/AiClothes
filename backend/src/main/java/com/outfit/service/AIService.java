package com.outfit.service;

import com.outfit.entity.User;

public interface AIService {
    String generateOutfitPlan(User user, String userInput, String weather, String temp, String city, String season, String regionType, String sessionContext);
}