// src/main/java/com/wms/backend/service/IInventoryService.java
package com.wms.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.backend.entity.Inventory;

import java.util.List;
import java.util.Map;

public interface IInventoryService extends IService<Inventory> {
    Inventory getByWarehouseAndMaterial(Long warehouseId, Long materialId);
    boolean increaseQuantity(Long warehouseId, Long materialId, Integer quantity);
    boolean decreaseQuantity(Long warehouseId, Long materialId, Integer quantity);
    boolean checkStockSufficient(Long warehouseId, Long materialId, Integer requiredQuantity);
    List<Map<String, Object>> getStockAlerts();
    Integer getTotalQuantityByWarehouseId(Long warehouseId);
    boolean lockQuantity(Long warehouseId, Long materialId, Integer quantity);
    boolean unlockQuantity(Long warehouseId, Long materialId, Integer quantity);
}
