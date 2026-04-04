// src/main/java/com/wms/backend/entity/InboundOrder.java
package com.wms.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("inbound_order")
public class InboundOrder {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String orderNo; // 入库单号
    private Long warehouseId; // 仓库ID
    private Long supplierId; // 供应商ID
    private BigDecimal totalAmount; // 总金额
    private Integer status; // 状态 0-待审核 1-已审核 2-已完成
    private Long operatorId; // 操作员ID
    private LocalDate receiveDate; // 接收日期
    private String remark; // 备注

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}