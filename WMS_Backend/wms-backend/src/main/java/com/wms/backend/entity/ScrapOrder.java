package com.wms.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("scrap_order")
public class ScrapOrder {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String orderNo;      // 报损单号
    private Long warehouseId;    // 仓库ID
    private String scrapType;    // 类型: DAMAGE-报损 SCRAP-报废
    private Integer status;      // 状态 0-待审核 1-已审核 2-已完成
    private Long operatorId;     // 操作员ID
    private String reason;       // 报损原因
    private String remark;       // 备注

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
