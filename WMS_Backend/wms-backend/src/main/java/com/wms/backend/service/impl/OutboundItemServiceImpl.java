// src/main/java/com/wms/backend/service/impl/OutboundItemServiceImpl.java
package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.backend.entity.OutboundItem;
import com.wms.backend.mapper.OutboundItemMapper;
import com.wms.backend.service.IOutboundItemService;
import org.springframework.stereotype.Service;

@Service
public class OutboundItemServiceImpl extends ServiceImpl<OutboundItemMapper, OutboundItem>
        implements IOutboundItemService {
    // 继承已提供基础CRUD实现
}