// src/main/java/com/wms/backend/entity/OutboundOrder.java
package com.wms.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("outbound_order")
public class OutboundOrder {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String orderNo; // 出库单号
    private Long warehouseId; // 仓库ID
    private Long customerId; // 客户ID
    private String recipient; // 领用人
    private String department; // 部门
    private BigDecimal totalAmount; // 总金额
    private Integer status; // 状态 0-待审核 1-已审核 2-已完成
    private Long operatorId; // 操作员ID
    private LocalDate issueDate; // 发放日期
    private String remark; // 备注

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}