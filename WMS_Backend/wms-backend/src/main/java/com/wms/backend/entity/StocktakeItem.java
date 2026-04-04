package com.wms.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("stocktake_item")
public class StocktakeItem {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long orderId;          // 盘点单ID
    private Long materialId;       // 物资ID
    private Integer systemQuantity; // 系统数量
    private Integer actualQuantity; // 实盘数量
    private Integer diffQuantity;   // 差异数量
    private String remark;          // 备注
}
