package com.wms.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.backend.common.Result;
import com.wms.backend.entity.InventoryTransaction;
import com.wms.backend.service.IInventoryTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory-transaction")
public class InventoryTransactionController {

    @Autowired
    private IInventoryTransactionService inventoryTransactionService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) Long warehouseId,
                       @RequestParam(required = false) Long materialId,
                       @RequestParam(required = false) String changeType,
                       @RequestParam(required = false) String referenceType,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate) {
        Page<InventoryTransaction> page = new Page<>(current, size);
        QueryWrapper<InventoryTransaction> wrapper = new QueryWrapper<>();
        if (warehouseId != null) wrapper.eq("warehouse_id", warehouseId);
        if (materialId != null) wrapper.eq("material_id", materialId);
        if (changeType != null && !changeType.isEmpty()) wrapper.eq("change_type", changeType);
        if (referenceType != null && !referenceType.isEmpty()) wrapper.eq("reference_type", referenceType);
        if (startDate != null && !startDate.isEmpty()) wrapper.ge("create_time", startDate + " 00:00:00");
        if (endDate != null && !endDate.isEmpty()) wrapper.le("create_time", endDate + " 23:59:59");
        wrapper.orderByDesc("create_time");
        return Result.success(inventoryTransactionService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(inventoryTransactionService.getById(id));
    }
}