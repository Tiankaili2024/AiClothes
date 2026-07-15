package com.outfit.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@TableName("sys_operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId; private Integer logType; private String operation;
    private String requestParam; private String result; private String ip; private Integer costTime;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}