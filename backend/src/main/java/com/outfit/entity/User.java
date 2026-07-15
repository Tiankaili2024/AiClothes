package com.outfit.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO) private Long id;
    private String username; private String password; private String nickname; private String avatar;
    private String gender; private String height; private String weight;
    private String bodyType; private String skinTone;
    private String stylePref; private String colorPref; private String fashionBan;
    private String securityQuestion; private String securityAnswer;
    private String city; private Integer role; private Integer status; private Integer dailyCount;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}