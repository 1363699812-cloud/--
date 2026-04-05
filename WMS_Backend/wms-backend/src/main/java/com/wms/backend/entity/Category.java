// src/main/java/com/wms/backend/entity/Category.java
package com.wms.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;          // 分类名称
    private String code;          // 分类编码
    private String description;   // 分类描述
    private Long parentId;        // 父级分类ID（支持多级分类）
    private Integer sortOrder;    // 排序
    private Integer status;        // 状态 (1启用/0停用)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}