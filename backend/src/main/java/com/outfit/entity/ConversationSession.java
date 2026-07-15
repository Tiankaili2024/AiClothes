package com.outfit.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@TableName("conversation_session")
public class ConversationSession {
    @TableId(type = IdType.AUTO) private Long id;
    private String sessionId; private Long userId; private String contextJson; private Integer currentScheme;
    private LocalDateTime expireTime;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}