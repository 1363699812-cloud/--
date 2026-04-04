// src/main/java/com/wms/backend/service/impl/WarehouseServiceImpl.java
package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.backend.entity.Warehouse;
import com.wms.backend.mapper.WarehouseMapper;
import com.wms.backend.service.IWarehouseService;
import org.springframework.stereotype.Service;

@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements IWarehouseService {
    // 继承ServiceImpl已提供基础CRUD功能
}