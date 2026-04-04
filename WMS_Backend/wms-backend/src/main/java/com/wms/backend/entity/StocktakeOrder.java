package com.wms.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("stocktake_order")
public class StocktakeOrder {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String orderNo;      // 盘点单号
    private Long warehouseId;    // 仓库ID
    private Integer status;      // 状态 0-进行中 1-已完成
    private Long operatorId;     // 操作员ID
    private String remark;       // 备注

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
