package com.wms.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;        // 操作用户ID
    private String username;    // 操作用户名
    private String module;      // 操作模块
    private String operation;   // 操作描述
    private String method;      // 请求方法
    private String params;      // 请求参数
    private String ip;          // 请求IP

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
