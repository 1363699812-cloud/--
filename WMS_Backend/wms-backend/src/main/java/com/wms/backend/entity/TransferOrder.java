package com.wms.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("transfer_order")
public class TransferOrder {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String orderNo;          // 调拨单号
    private Long fromWarehouseId;    // 源仓库ID
    private Long toWarehouseId;      // 目标仓库ID
    private Integer status;          // 状态 0-待审核 1-已审核 2-已完成
    private Long operatorId;         // 操作员ID
    private String remark;           // 备注

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
