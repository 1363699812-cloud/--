// src/main/java/com/wms/backend/controller/InventoryController.java
package com.wms.backend.controller;

import com.wms.backend.annotation.Log;
import com.wms.backend.common.Result;
import com.wms.backend.entity.Inventory;
import com.wms.backend.service.IInventoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private IInventoryService inventoryService;

    // 1. 查询库存（分页）
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) Long warehouseId,
                       @RequestParam(required = false) Long materialId) {
        Page<Inventory> page = new Page<>(current, size);
        QueryWrapper<Inventory> wrapper = new QueryWrapper<>();
        if (warehouseId != null) wrapper.eq("warehouse_id", warehouseId);
        if (materialId != null) wrapper.eq("material_id", materialId);
        return Result.success(inventoryService.page(page, wrapper));
    }

    // 2. 根据ID查询库存
    @GetMapping("/{id}")
    public Result<Inventory> getById(@PathVariable Long id) {
        Inventory inventory = inventoryService.getById(id);
        return Result.success(inventory);
    }

    // 3. 根据仓库ID和物资ID查询库存
    @GetMapping("/detail")
    public Result<Inventory> getByWarehouseAndMaterial(
            @RequestParam Long warehouseId,
            @RequestParam Long materialId) {
        Inventory inventory = inventoryService.getByWarehouseAndMaterial(warehouseId, materialId);
        return Result.success(inventory);
    }

    // 4. 新增库存
    @PostMapping
    public Result<Boolean> save(@RequestBody Inventory inventory) {
        boolean save = inventoryService.save(inventory);
        return Result.success(save);
    }

    // 5. 修改库存
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Inventory inventory) {
        inventory.setId(id);
        boolean update = inventoryService.updateById(inventory);
        return Result.success(update);
    }

    // 6. 删除库存
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean remove = inventoryService.removeById(id);
        return Result.success(remove);
    }

    // 7. 增加库存数量
    @Log(value = "增加库存", module = "库存管理")
    @PostMapping("/increase")
    public Result<Boolean> increaseQuantity(
            @RequestParam Long warehouseId,
            @RequestParam Long materialId,
            @RequestParam Integer quantity) {
        boolean result = inventoryService.increaseQuantity(warehouseId, materialId, quantity);
        return result ? Result.success(true) : Result.error("增加库存失败");
    }

    // 8. 减少库存数量
    @Log(value = "减少库存", module = "库存管理")
    @PostMapping("/decrease")
    public Result<Boolean> decreaseQuantity(
            @RequestParam Long warehouseId,
            @RequestParam Long materialId,
            @RequestParam Integer quantity) {
        boolean result = inventoryService.decreaseQuantity(warehouseId, materialId, quantity);
        return result ? Result.success(true) : Result.error("减少库存失败，可能库存不足");
    }

    // 9. 检查库存是否充足
    @GetMapping("/check-stock")
    public Result<Boolean> checkStockSufficient(
            @RequestParam Long warehouseId,
            @RequestParam Long materialId,
            @RequestParam Integer requiredQuantity) {
        boolean sufficient = inventoryService.checkStockSufficient(warehouseId, materialId, requiredQuantity);
        return Result.success(sufficient);
    }

    // 10. 库存预警查询
    @GetMapping("/alerts")
    public Result getStockAlerts() {
        List<Map<String, Object>> alerts = inventoryService.getStockAlerts();
        return Result.success(alerts);
    }
}