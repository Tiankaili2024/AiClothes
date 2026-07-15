package com.outfit.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@TableName("outfit_record")
public class OutfitRecord {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId; private String sessionId; private Integer schemeIndex;
    private String userInput; private String weatherInfo; private String parsedParams; private String prompt; private String imageUrl;
    private String styleTags; private String topItem; private String bottomItem; private String shoesItem; private String accessoryItem;
    private String matchReason;
    private Integer scoreBody; private Integer scoreWeather; private Integer scoreOccasion; private Integer scoreColor; private Integer scoreTotal;
    private String optimizeAdvice;
    private Integer status; private String failReason;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}