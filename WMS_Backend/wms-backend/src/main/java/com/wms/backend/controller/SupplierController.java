package com.wms.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.backend.common.Result;
import com.wms.backend.entity.Supplier;
import com.wms.backend.service.ISupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import com.wms.backend.annotation.Log;
import com.wms.backend.annotation.RequireRole;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier")
public class SupplierController {

    @Autowired
    private ISupplierService supplierService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String code) {
        Page<Supplier> page = new Page<>(current, size);
        QueryWrapper<Supplier> wrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (code != null && !code.isEmpty()) {
            wrapper.like("code", code);
        }
        wrapper.orderByDesc("status").orderByDesc("create_time");
        return Result.success(supplierService.page(page, wrapper));
    }

    @GetMapping("/all")
    public Result all() {
        QueryWrapper<Supplier> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        return Result.success(supplierService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(supplierService.getById(id));
    }

    @Log(value = "新增供应商", module = "供应商管理")
    @PostMapping
    @RequireRole({"admin", "warehouse_keeper", "purchaser"})
    public Result save(@RequestBody Supplier supplier) {
        return supplierService.save(supplier) ? Result.success("创建成功") : Result.error("创建失败");
    }

    @Log(value = "修改供应商", module = "供应商管理")
    @PutMapping("/{id}")
    @RequireRole({"admin", "warehouse_keeper", "purchaser"})
    public Result update(@PathVariable Long id, @RequestBody Supplier supplier) {
        supplier.setId(id);
        return supplierService.updateById(supplier) ? Result.success("更新成功") : Result.error("更新失败");
    }

    @Log(value = "删除供应商", module = "供应商管理")
    @DeleteMapping("/{id}")
    @RequireRole({"admin", "warehouse_keeper", "purchaser"})
    public Result delete(@PathVariable Long id) {
        Supplier supplier = supplierService.getById(id);
        if (supplier == null) {
            return Result.error("供应商不存在");
        }
        if (supplier.getStatus() == 1) {
            return Result.error("只能删除停用状态的供应商");
        }
        return supplierService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }
}
