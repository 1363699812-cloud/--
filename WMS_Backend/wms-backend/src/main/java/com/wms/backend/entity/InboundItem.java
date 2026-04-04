// src/main/java/com/wms/backend/entity/InboundItem.java
package com.wms.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("inbound_item")
public class InboundItem {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long orderId; // 订单ID
    private Long materialId; // 物资ID
    private Integer quantity; // 数量
    private BigDecimal unitPrice; // 单价
    private BigDecimal amount; // 金额
    private String batchNumber; // 批次号
    private LocalDate productionDate; // 生产日期
    private LocalDate expiryDate; // 有效期

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}