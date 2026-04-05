// src/main/java/com/wms/backend/controller/CategoryController.java
package com.wms.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.backend.annotation.Log;
import com.wms.backend.annotation.RequireRole;
import com.wms.backend.common.Result;
import com.wms.backend.entity.Category;
import com.wms.backend.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    /**
     * 获取分类列表（分页）
     */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String description) {
        Page<Category> page = new Page<>(current, size);
        QueryWrapper<Category> wrapper = new QueryWrapper<>();

        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (description != null && !description.isEmpty()) {
            wrapper.like("description", description);
        }
        wrapper.orderByDesc("status").orderByDesc("created_at");

        Page<Category> result = categoryService.page(page, wrapper);
        return Result.success(result);
    }

    /**
     * 获取所有分类（不分页，用于下拉选择）
     */
    @GetMapping("/all")
    public Result getAllCategories() {
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByAsc("sort_order");
        List<Category> categories = categoryService.list(wrapper);
        return Result.success(categories);
    }

    /**
     * 根据ID获取分类详情
     */
    @GetMapping("/{id}")
    public Result getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category != null) {
            return Result.success(category);
        }
        return Result.error("分类不存在");
    }

    /**
     * 创建分类
     */
    @Log(value = "创建分类", module = "分类管理")
    @PostMapping
    @RequireRole({"admin", "warehouse_keeper"})
    public Result createCategory(@RequestBody Category category) {
        // 检查分类名称是否已存在
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.eq("name", category.getName());
        if (categoryService.count(wrapper) > 0) {
            return Result.error("分类名称已存在");
        }

        boolean saved = categoryService.save(category);
        if (saved) {
            return Result.success("分类创建成功");
        }
        return Result.error("分类创建失败");
    }

    /**
     * 更新分类信息
     */
    @Log(value = "修改分类", module = "分类管理")
    @PutMapping("/{id}")
    @RequireRole({"admin", "warehouse_keeper"})
    public Result updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);

        // 检查分类名称是否与其他分类冲突
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.eq("name", category.getName()).ne("id", id);
        if (categoryService.count(wrapper) > 0) {
            return Result.error("分类名称已存在");
        }

        boolean updated = categoryService.updateById(category);
        if (updated) {
            return Result.success("分类更新成功");
        }
        return Result.error("分类更新失败");
    }

    /**
     * 删除分类
     */
    @Log(value = "删除分类", module = "分类管理")
    @DeleteMapping("/{id}")
    @RequireRole({"admin", "warehouse_keeper"})
    public Result deleteCategory(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }
        if (category.getStatus() == 1) {
            return Result.error("只能删除停用状态的分类");
        }
        boolean deleted = categoryService.removeById(id);
        if (deleted) {
            return Result.success("分类删除成功");
        }
        return Result.error("分类删除失败");
    }

    /**
     * 批量删除分类
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean deleted = categoryService.removeBatchByIds(ids);
        if (deleted) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }
}