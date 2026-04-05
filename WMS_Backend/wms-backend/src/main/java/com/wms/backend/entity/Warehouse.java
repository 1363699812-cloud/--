// src/main/java/com/wms/backend/entity/Warehouse.java
package com.wms.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("warehouse")
public class Warehouse {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;          // 仓库名称
    private String code;          // 仓库编码
    private String address;       // 仓库地址
    private String contactPerson; // 联系人
    private String phone;         // 联系电话
    private String description;   // 描述
    private Integer capacity;     // 容量
    private Integer status;        // 状态 (1启用/0停用)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}