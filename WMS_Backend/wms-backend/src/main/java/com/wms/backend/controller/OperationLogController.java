package com.wms.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.backend.common.Result;
import com.wms.backend.entity.OperationLog;
import com.wms.backend.service.IOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operation-log")
public class OperationLogController {

    @Autowired
    private IOperationLogService operationLogService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) String username,
                       @RequestParam(required = false) String module) {
        Page<OperationLog> page = new Page<>(current, size);
        QueryWrapper<OperationLog> wrapper = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) wrapper.like("username", username);
        if (module != null && !module.isEmpty()) wrapper.eq("module", module);
        wrapper.orderByDesc("create_time");
        return Result.success(operationLogService.page(page, wrapper));
    }
}
