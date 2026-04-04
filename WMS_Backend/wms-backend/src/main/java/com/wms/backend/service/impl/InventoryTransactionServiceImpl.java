// src/main/java/com/wms/backend/service/impl/InventoryTransactionServiceImpl.java
package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.backend.entity.InventoryTransaction;
import com.wms.backend.mapper.InventoryTransactionMapper;
import com.wms.backend.service.IInventoryTransactionService;
import org.springframework.stereotype.Service;

@Service
public class InventoryTransactionServiceImpl extends ServiceImpl<InventoryTransactionMapper, InventoryTransaction>
        implements IInventoryTransactionService {
    // 继承已提供基础CRUD实现
}