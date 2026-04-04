package com.wms.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("scrap_item")
public class ScrapItem {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long orderId;      // 报损单ID
    private Long materialId;   // 物资ID
    private Integer quantity;  // 报损数量
    private String remark;     // 备注
}
