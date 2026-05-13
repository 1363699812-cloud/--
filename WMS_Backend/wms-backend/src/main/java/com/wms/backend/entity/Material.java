// src/main/java/com/wms/backend/entity/Material.java
package com.wms.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("material")
public class Material {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String code; // 物资编码
    private String name; // 物资名称
    private Long categoryId; // 类别ID
    private String unit; // 单位
    private String specification; // 规格型号
    private Integer minStock; // 最小库存
    private Integer maxStock; // 最大库存
    private Integer safetyStock; // 安全库存
    private BigDecimal holdingCost; // 单位年持有成本
    private BigDecimal orderCost; // 单次订货成本
    private Integer reorderPoint; // 补货点 ROP = d̄ × L + SS
    private Integer eoq; // 经济订货批量 sqrt(2DS/H)
    private BigDecimal dailyDemand; // 日均需求量(统计值)
    private Integer leadTime; // 补货提前期(天)
    private String description; // 描述
    private Integer status; // 状态 1-启用 0-禁用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}