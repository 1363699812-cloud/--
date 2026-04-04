// src/main/java/com/wms/backend/entity/Inventory.java
package com.wms.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("inventory")
public class Inventory {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long warehouseId; // 仓库ID

    private Long materialId; // 物资ID

    private Integer quantity; // 库存数量

    private Integer lockQuantity; // 锁定数量(预占库存)

    @TableField(value = "version", update = "%s+1")
    private Long version; // 乐观锁版本号

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
