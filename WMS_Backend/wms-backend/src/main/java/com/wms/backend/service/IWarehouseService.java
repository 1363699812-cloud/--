// src/main/java/com/wms/backend/service/IWarehouseService.java
package com.wms.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.backend.entity.Warehouse;

public interface IWarehouseService extends IService<Warehouse> {
    // 继承IService已提供基础CRUD功能
}