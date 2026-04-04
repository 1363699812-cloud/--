// src/main/java/com/wms/backend/service/impl/InboundItemServiceImpl.java
package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.backend.entity.InboundItem;
import com.wms.backend.mapper.InboundItemMapper;
import com.wms.backend.service.IInboundItemService;
import org.springframework.stereotype.Service;

@Service
public class InboundItemServiceImpl extends ServiceImpl<InboundItemMapper, InboundItem>
        implements IInboundItemService {
    // 继承已提供基础CRUD实现
}