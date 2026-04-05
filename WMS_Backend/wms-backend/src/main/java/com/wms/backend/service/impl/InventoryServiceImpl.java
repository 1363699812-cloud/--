// src/main/java/com/wms/backend/service/impl/InventoryServiceImpl.java
package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.backend.entity.Inventory;
import com.wms.backend.mapper.InventoryMapper;
import com.wms.backend.service.IInventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements IInventoryService {

    @Override
    public Inventory getByWarehouseAndMaterial(Long warehouseId, Long materialId) {
        QueryWrapper<Inventory> wrapper = new QueryWrapper<>();
        wrapper.eq("warehouse_id", warehouseId)
                .eq("material_id", materialId);
        return this.getOne(wrapper);
    }

    @Override
    @Transactional
    public boolean increaseQuantity(Long warehouseId, Long materialId, Integer quantity) {
        if (quantity <= 0) {
            return false;
        }

        Inventory inventory = this.getByWarehouseAndMaterial(warehouseId, materialId);
        if (inventory == null) {
            // 如果不存在库存记录，则创建新的库存记录
            inventory = new Inventory();
            inventory.setWarehouseId(warehouseId);
            inventory.setMaterialId(materialId);
            inventory.setQuantity(quantity);
            inventory.setLockQuantity(0);
            inventory.setVersion(0L);
            return this.save(inventory);
        } else {
            // 存在则更新库存数量
            inventory.setQuantity(inventory.getQuantity() + quantity);
            return this.updateById(inventory);
        }
    }

    @Override
    @Transactional
    public boolean decreaseQuantity(Long warehouseId, Long materialId, Integer quantity) {
        if (quantity <= 0) {
            return false;
        }

        Inventory inventory = this.getByWarehouseAndMaterial(warehouseId, materialId);
        if (inventory == null || inventory.getQuantity() < quantity) {
            // 库存不足，扣减失败
            return false;
        }

        // 扣减库存
        inventory.setQuantity(inventory.getQuantity() - quantity);
        return this.updateById(inventory);
    }

    @Override
    public boolean checkStockSufficient(Long warehouseId, Long materialId, Integer requiredQuantity) {
        if (requiredQuantity <= 0) {
            return true;
        }

        Inventory inventory = this.getByWarehouseAndMaterial(warehouseId, materialId);
        if (inventory == null) {
            return false;
        }
        int available = inventory.getQuantity() - (inventory.getLockQuantity() != null ? inventory.getLockQuantity() : 0);
        return available >= requiredQuantity;
    }

    @Override
    public List<Map<String, Object>> getStockAlerts() {
        return this.baseMapper.selectStockAlerts();
    }

    @Override
    public Integer getTotalQuantityByWarehouseId(Long warehouseId) {
        return this.baseMapper.selectTotalQuantityByWarehouseId(warehouseId);
    }

    @Override
    @Transactional
    public boolean lockQuantity(Long warehouseId, Long materialId, Integer quantity) {
        if (quantity <= 0) return false;
        Inventory inventory = this.getByWarehouseAndMaterial(warehouseId, materialId);
        if (inventory == null) return false;
        int available = inventory.getQuantity() - (inventory.getLockQuantity() != null ? inventory.getLockQuantity() : 0);
        if (available < quantity) return false;
        inventory.setLockQuantity((inventory.getLockQuantity() != null ? inventory.getLockQuantity() : 0) + quantity);
        return this.updateById(inventory);
    }

    @Override
    @Transactional
    public boolean unlockQuantity(Long warehouseId, Long materialId, Integer quantity) {
        if (quantity <= 0) return false;
        Inventory inventory = this.getByWarehouseAndMaterial(warehouseId, materialId);
        if (inventory == null) return false;
        int currentLock = inventory.getLockQuantity() != null ? inventory.getLockQuantity() : 0;
        inventory.setLockQuantity(Math.max(0, currentLock - quantity));
        return this.updateById(inventory);
    }
}
