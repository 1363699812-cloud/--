package com.wms.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.backend.common.Result;
import com.wms.backend.entity.Customer;
import com.wms.backend.service.ICustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import com.wms.backend.annotation.Log;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private ICustomerService customerService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String code) {
        Page<Customer> page = new Page<>(current, size);
        QueryWrapper<Customer> wrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (code != null && !code.isEmpty()) {
            wrapper.like("code", code);
        }
        wrapper.orderByDesc("create_time");
        return Result.success(customerService.page(page, wrapper));
    }

    @GetMapping("/all")
    public Result all() {
        QueryWrapper<Customer> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        return Result.success(customerService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(customerService.getById(id));
    }

    @Log(value = "新增客户", module = "客户管理")
    @PostMapping
    public Result save(@RequestBody Customer customer) {
        return customerService.save(customer) ? Result.success("创建成功") : Result.error("创建失败");
    }

    @Log(value = "修改客户", module = "客户管理")
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Customer customer) {
        customer.setId(id);
        return customerService.updateById(customer) ? Result.success("更新成功") : Result.error("更新失败");
    }

    @Log(value = "删除客户", module = "客户管理")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return customerService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }
}
