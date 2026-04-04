package com.wms.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("customer")
public class Customer {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String code;           // 客户编码
    private String name;           // 客户名称
    private String contactPerson;  // 联系人
    private String phone;          // 联系电话
    private String email;          // 邮箱
    private String address;        // 地址
    private Integer status;        // 状态 1-启用 0-禁用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
