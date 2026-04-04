package com.wms.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("transfer_item")
public class TransferItem {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long orderId;      // 调拨单ID
    private Long materialId;   // 物资ID
    private Integer quantity;  // 调拨数量
    private String remark;     // 备注
}
