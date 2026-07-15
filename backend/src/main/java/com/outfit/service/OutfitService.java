package com.outfit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.outfit.dto.OutfitRequestDTO;
import com.outfit.entity.OutfitRecord;
import java.util.Map;

public interface OutfitService {
    OutfitRecord generateOutfit(Long userId, OutfitRequestDTO dto);
    OutfitRecord getRecordDetail(Long recordId, Long userId);
    Page<OutfitRecord> getUserRecords(Long userId, int page, int size);
    void deleteRecord(Long recordId, Long userId);
    void clearRecords(Long userId);
    Long getTodayCount(Long userId);
    Object quickRefine(Long userId, com.outfit.dto.QuickRefineDTO dto);
    Object getSessionRecords(Long userId, String sessionId);
    Map<String,Object> getUserPortrait(Long userId);
}
