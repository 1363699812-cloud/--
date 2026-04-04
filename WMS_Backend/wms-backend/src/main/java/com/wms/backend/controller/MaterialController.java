// src/main/java/com/wms/backend/controller/MaterialController.java
package com.wms.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.backend.annotation.Log;
import com.wms.backend.common.Result;
import com.wms.backend.entity.Material;
import com.wms.backend.service.IMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/material")
public class MaterialController {

    @Autowired
    private IMaterialService materialService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String code,
                       @RequestParam(required = false) Long categoryId) {
        Page<Material> page = new Page<>(current, size);
        QueryWrapper<Material> wrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) wrapper.like("name", name);
        if (code != null && !code.isEmpty()) wrapper.like("code", code);
        if (categoryId != null) wrapper.eq("category_id", categoryId);
        wrapper.orderByDesc("create_time");
        return Result.success(materialService.page(page, wrapper));
    }

    @GetMapping("/all")
    public Result all() {
        QueryWrapper<Material> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        return Result.success(materialService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(materialService.getById(id));
    }

    @Log(value = "新增物资", module = "物资管理")
    @PostMapping
    public Result save(@RequestBody Material material) {
        return materialService.save(material) ? Result.success("创建成功") : Result.error("创建失败");
    }

    @Log(value = "修改物资", module = "物资管理")
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Material material) {
        material.setId(id);
        return materialService.updateById(material) ? Result.success("更新成功") : Result.error("更新失败");
    }

    @Log(value = "删除物资", module = "物资管理")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return materialService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }
}