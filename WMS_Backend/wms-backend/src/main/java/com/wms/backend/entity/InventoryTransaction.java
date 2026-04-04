// src/main/java/com/wms/backend/entity/InventoryTransaction.java
package com.wms.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("inventory_transaction")
public class InventoryTransaction {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long warehouseId; // 仓库ID
    private Long materialId; // 物资ID
    private Integer quantity; // 变化数量（正数表示增加，负数表示减少）
    private String changeType; // 变化类型 IN-入库, OUT-出库
    private String referenceType; // 关联类型 INBOUND-入库单, OUTBOUND-出库单
    private String referenceId; // 关联ID
    private String remark; // 备注

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;
}