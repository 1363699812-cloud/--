// src/main/java/com/wms/backend/mapper/InboundOrderMapper.java
package com.wms.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.backend.entity.InboundOrder;

public interface InboundOrderMapper extends BaseMapper<InboundOrder> {
    // 继承BaseMapper已提供基础CRUD功能
    // 可在此添加自定义查询方法
}