// src/main/java/com/wms/backend/controller/WarehouseController.java
package com.wms.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.backend.annotation.Log;
import com.wms.backend.common.Result;
import com.wms.backend.entity.Warehouse;
import com.wms.backend.service.IWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

    @Autowired
    private IWarehouseService warehouseService;

    /**
     * 获取仓库列表（分页）
     */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String address) {
        Page<Warehouse> page = new Page<>(current, size);
        QueryWrapper<Warehouse> wrapper = new QueryWrapper<>();

        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (address != null && !address.isEmpty()) {
            wrapper.like("address", address);
        }

        Page<Warehouse> result = warehouseService.page(page, wrapper);
        return Result.success(result);
    }

    /**
     * 获取所有仓库（不分页，用于下拉选择）
     */
    @GetMapping("/all")
    public Result getAllWarehouses() {
        List<Warehouse> warehouses = warehouseService.list();
        return Result.success(warehouses);
    }

    /**
     * 根据ID获取仓库详情
     */
    @GetMapping("/{id}")
    public Result getWarehouseById(@PathVariable Long id) {
        Warehouse warehouse = warehouseService.getById(id);
        if (warehouse != null) {
            return Result.success(warehouse);
        }
        return Result.error("仓库不存在");
    }

    /**
     * 创建仓库
     */
    @Log(value = "创建仓库", module = "仓库管理")
    @PostMapping
    public Result createWarehouse(@RequestBody Warehouse warehouse) {
        // 校验仓库容量
        if (warehouse.getCapacity() == null || warehouse.getCapacity() < 1) {
            return Result.error("仓库容量不能小于1");
        }

        // 检查仓库名称是否已存在
        QueryWrapper<Warehouse> wrapper = new QueryWrapper<>();
        wrapper.eq("name", warehouse.getName());
        if (warehouseService.count(wrapper) > 0) {
            return Result.error("仓库名称已存在");
        }

        boolean saved = warehouseService.save(warehouse);
        if (saved) {
            return Result.success("仓库创建成功");
        }
        return Result.error("仓库创建失败");
    }

    /**
     * 更新仓库信息
     */
    @Log(value = "修改仓库", module = "仓库管理")
    @PutMapping("/{id}")
    public Result updateWarehouse(@PathVariable Long id, @RequestBody Warehouse warehouse) {
        warehouse.setId(id);

        // 校验仓库容量
        if (warehouse.getCapacity() == null || warehouse.getCapacity() < 1) {
            return Result.error("仓库容量不能小于1");
        }

        // 检查仓库名称是否与其他仓库冲突
        QueryWrapper<Warehouse> wrapper = new QueryWrapper<>();
        wrapper.eq("name", warehouse.getName()).ne("id", id);
        if (warehouseService.count(wrapper) > 0) {
            return Result.error("仓库名称已存在");
        }

        boolean updated = warehouseService.updateById(warehouse);
        if (updated) {
            return Result.success("仓库更新成功");
        }
        return Result.error("仓库更新失败");
    }

    /**
     * 删除仓库
     */
    @Log(value = "删除仓库", module = "仓库管理")
    @DeleteMapping("/{id}")
    public Result deleteWarehouse(@PathVariable Long id) {
        boolean deleted = warehouseService.removeById(id);
        if (deleted) {
            return Result.success("仓库删除成功");
        }
        return Result.error("仓库删除失败");
    }

    /**
     * 批量删除仓库
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean deleted = warehouseService.removeBatchByIds(ids);
        if (deleted) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }
}