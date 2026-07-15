package com.outfit.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@TableName("clothes_collect")
public class OutfitFavorite {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId; private Long recordId;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}