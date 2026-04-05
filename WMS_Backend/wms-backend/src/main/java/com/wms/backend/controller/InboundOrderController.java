// src/main/java/com/wms/backend/controller/InboundOrderController.java
package com.wms.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.backend.annotation.Log;
import com.wms.backend.annotation.RequireRole;
import com.wms.backend.common.Result;
import com.wms.backend.entity.InboundItem;
import com.wms.backend.entity.InboundOrder;
import com.wms.backend.service.IInboundItemService;
import com.wms.backend.service.IInboundOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inbound-order")
public class InboundOrderController {

    @Autowired
    private IInboundOrderService inboundOrderService;

    @Autowired
    private IInboundItemService inboundItemService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(required = false) String orderNo,
                       @RequestParam(required = false) Long supplierId,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate) {
        Page<InboundOrder> page = new Page<>(current, size);
        QueryWrapper<InboundOrder> wrapper = new QueryWrapper<>();
        if (status != null) wrapper.eq("status", status);
        if (orderNo != null && !orderNo.isEmpty()) wrapper.like("order_no", orderNo);
        if (supplierId != null) wrapper.eq("supplier_id", supplierId);
        if (startDate != null && !startDate.isEmpty()) wrapper.ge("create_time", startDate + " 00:00:00");
        if (endDate != null && !endDate.isEmpty()) wrapper.le("create_time", endDate + " 23:59:59");
        wrapper.orderByDesc("create_time");
        return Result.success(inboundOrderService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(inboundOrderService.getById(id));
    }

    @GetMapping("/{orderId}/items")
    public Result getItems(@PathVariable Long orderId) {
        QueryWrapper<InboundItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        return Result.success(inboundItemService.list(wrapper));
    }

    @Log(value = "创建入库单", module = "入库管理")
    @PostMapping
    @RequireRole({"admin", "warehouse_keeper", "purchaser"})
    public Result create(@RequestBody CreateInboundOrderRequest request) {
        boolean result = inboundOrderService.createInboundOrder(request.getOrder(), request.getItems());
        return result ? Result.success("创建成功") : Result.error("创建入库单失败");
    }

    @Log(value = "审核入库单", module = "入库管理")
    @PostMapping("/{orderId}/audit")
    @RequireRole({"admin", "warehouse_keeper"})
    public Result audit(@PathVariable Long orderId) {
        try {
            boolean result = inboundOrderService.auditInboundOrder(orderId);
            return result ? Result.success("审核成功") : Result.error("审核入库单失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Log(value = "驳回入库单", module = "入库管理")
    @PostMapping("/{orderId}/reject")
    @RequireRole({"admin", "warehouse_keeper"})
    public Result reject(@PathVariable Long orderId) {
        boolean result = inboundOrderService.rejectInboundOrder(orderId);
        return result ? Result.success("已驳回") : Result.error("驳回失败");
    }

    @Log(value = "完成入库单", module = "入库管理")
    @PostMapping("/{orderId}/complete")
    @RequireRole({"admin", "warehouse_keeper"})
    public Result complete(@PathVariable Long orderId) {
        try {
            boolean result = inboundOrderService.completeInboundOrder(orderId);
            return result ? Result.success("完成入库") : Result.error("完成入库单失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Log(value = "修改入库单", module = "入库管理")
    @PutMapping("/{id}")
    @RequireRole({"admin", "warehouse_keeper", "purchaser"})
    public Result update(@PathVariable Long id, @RequestBody CreateInboundOrderRequest request) {
        try {
            boolean result = inboundOrderService.updateInboundOrder(id, request.getOrder(), request.getItems());
            return result ? Result.success("更新成功") : Result.error("更新失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Log(value = "删除入库单", module = "入库管理")
    @DeleteMapping("/{id}")
    @RequireRole({"admin", "warehouse_keeper", "purchaser"})
    public Result delete(@PathVariable Long id) {
        InboundOrder order = inboundOrderService.getById(id);
        if (order != null && order.getStatus() != 0) {
            return Result.error("只能删除待审核的入库单");
        }
        return inboundOrderService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    static class CreateInboundOrderRequest {
        private InboundOrder order;
        private List<InboundItem> items;
        public InboundOrder getOrder() { return order; }
        public void setOrder(InboundOrder order) { this.order = order; }
        public List<InboundItem> getItems() { return items; }
        public void setItems(List<InboundItem> items) { this.items = items; }
    }
}