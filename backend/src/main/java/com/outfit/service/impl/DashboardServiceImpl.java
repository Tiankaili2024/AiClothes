package com.outfit.service.impl;

import com.outfit.mapper.OutfitRecordMapper;
import com.outfit.mapper.UserMapper;
import com.outfit.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {
    @Autowired private UserMapper userMapper;
    @Autowired private OutfitRecordMapper recordMapper;

    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // 用户统计
        long totalUsers = userMapper.selectCount(null);
        data.put("totalUsers", totalUsers);

        // 生成统计
        long totalRecords = recordMapper.selectCount(null);
        data.put("totalRecords", totalRecords);

        // 今日生成
        long todayRecords = recordMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.outfit.entity.OutfitRecord>()
                .ge(com.outfit.entity.OutfitRecord::getCreatedAt, 
                    java.time.LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.MIN))
        );
        data.put("todayRecords", todayRecords);

        // 成功率
        long successCount = recordMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.outfit.entity.OutfitRecord>()
                .eq(com.outfit.entity.OutfitRecord::getStatus, 1)
        );
        data.put("successRate", totalRecords > 0 ? String.format("%.1f", successCount * 100.0 / totalRecords) : "0");

        // 风格分布
        data.put("styleDistribution", recordMapper.countByStyle());

        // 时段热度
        data.put("hourlyActivity", recordMapper.countByHour());

        return data;
    }

    @Override
    public Map<String,Object> getGlobalPortrait() {
        Map<String,Object> result = new HashMap<>();
        result.put("styleDistribution", recordMapper.countByStyle());
        result.put("hourlyActivity", recordMapper.countByHour());
        result.put("totalUsers", userMapper.selectCount(null));
        result.put("totalRecords", recordMapper.selectCount(null));
        return result;
    }
}
