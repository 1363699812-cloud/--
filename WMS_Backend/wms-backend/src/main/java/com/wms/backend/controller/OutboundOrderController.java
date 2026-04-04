// src/main/java/com/wms/backend/controller/OutboundOrderController.java
package com.wms.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.backend.annotation.Log;
import com.wms.backend.common.Result;
import com.wms.backend.entity.OutboundItem;
import com.wms.backend.entity.OutboundOrder;
import com.wms.backend.service.IOutboundItemService;
import com.wms.backend.service.IOutboundOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/outbound-order")
public class OutboundOrderController {

    @Autowired
    private IOutboundOrderService outboundOrderService;

    @Autowired
    private IOutboundItemService outboundItemService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(required = false) String orderNo,
                       @RequestParam(required = false) Long customerId,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate) {
        Page<OutboundOrder> page = new Page<>(current, size);
        QueryWrapper<OutboundOrder> wrapper = new QueryWrapper<>();
        if (status != null) wrapper.eq("status", status);
        if (orderNo != null && !orderNo.isEmpty()) wrapper.like("order_no", orderNo);
        if (customerId != null) wrapper.eq("customer_id", customerId);
        if (startDate != null && !startDate.isEmpty()) wrapper.ge("create_time", startDate + " 00:00:00");
        if (endDate != null && !endDate.isEmpty()) wrapper.le("create_time", endDate + " 23:59:59");
        wrapper.orderByDesc("create_time");
        return Result.success(outboundOrderService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(outboundOrderService.getById(id));
    }

    @GetMapping("/{orderId}/items")
    public Result getItems(@PathVariable Long orderId) {
        QueryWrapper<OutboundItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        return Result.success(outboundItemService.list(wrapper));
    }

    @Log(value = "创建出库单", module = "出库管理")
    @PostMapping
    public Result create(@RequestBody CreateOutboundOrderRequest request) {
        boolean result = outboundOrderService.createOutboundOrder(request.getOrder(), request.getItems());
        return result ? Result.success("创建成功") : Result.error("创建出库单失败");
    }

    @Log(value = "审核出库单", module = "出库管理")
    @PostMapping("/{orderId}/audit")
    public Result audit(@PathVariable Long orderId) {
        boolean result = outboundOrderService.auditOutboundOrder(orderId);
        return result ? Result.success("审核成功") : Result.error("审核出库单失败，可能库存不足");
    }

    @Log(value = "完成出库单", module = "出库管理")
    @PostMapping("/{orderId}/complete")
    public Result complete(@PathVariable Long orderId) {
        boolean result = outboundOrderService.completeOutboundOrder(orderId);
        return result ? Result.success("完成出库") : Result.error("完成出库单失败，可能库存不足");
    }

    @GetMapping("/{orderId}/check-stock")
    public Result checkStock(@PathVariable Long orderId) {
        boolean sufficient = outboundOrderService.checkStockForOutbound(orderId);
        return Result.success(sufficient);
    }

    @Log(value = "修改出库单", module = "出库管理")
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody OutboundOrder order) {
        order.setId(id);
        return outboundOrderService.updateById(order) ? Result.success("更新成功") : Result.error("更新失败");
    }

    @Log(value = "删除出库单", module = "出库管理")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        OutboundOrder order = outboundOrderService.getById(id);
        if (order != null && order.getStatus() != 0) {
            return Result.error("只能删除待审核的出库单");
        }
        return outboundOrderService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    static class CreateOutboundOrderRequest {
        private OutboundOrder order;
        private List<OutboundItem> items;
        public OutboundOrder getOrder() { return order; }
        public void setOrder(OutboundOrder order) { this.order = order; }
        public List<OutboundItem> getItems() { return items; }
        public void setItems(List<OutboundItem> items) { this.items = items; }
    }
}