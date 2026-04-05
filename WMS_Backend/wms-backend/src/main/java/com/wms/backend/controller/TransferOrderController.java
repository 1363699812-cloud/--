package com.wms.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.backend.annotation.Log;
import com.wms.backend.annotation.RequireRole;
import com.wms.backend.common.Result;
import com.wms.backend.entity.TransferItem;
import com.wms.backend.entity.TransferOrder;
import com.wms.backend.service.ITransferItemService;
import com.wms.backend.service.ITransferOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfer-order")
public class TransferOrderController {

    @Autowired
    private ITransferOrderService transferOrderService;

    @Autowired
    private ITransferItemService transferItemService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) Integer status) {
        Page<TransferOrder> page = new Page<>(current, size);
        QueryWrapper<TransferOrder> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        return Result.success(transferOrderService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(transferOrderService.getById(id));
    }

    @GetMapping("/{orderId}/items")
    public Result getItems(@PathVariable Long orderId) {
        QueryWrapper<TransferItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        return Result.success(transferItemService.list(wrapper));
    }

    @Log(value = "创建调拨单", module = "调拨管理")
    @PostMapping
    @RequireRole({"admin", "warehouse_keeper"})
    public Result create(@RequestBody CreateTransferRequest request) {
        boolean result = transferOrderService.createTransferOrder(request.getOrder(), request.getItems());
        return result ? Result.success("创建成功") : Result.error("创建失败");
    }

    @Log(value = "审核调拨单", module = "调拨管理")
    @PostMapping("/{orderId}/audit")
    @RequireRole({"admin", "warehouse_keeper"})
    public Result audit(@PathVariable Long orderId) {
        try {
            boolean result = transferOrderService.auditTransferOrder(orderId);
            return result ? Result.success("审核成功") : Result.error("审核失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Log(value = "驳回调拨单", module = "调拨管理")
    @PostMapping("/{orderId}/reject")
    @RequireRole({"admin", "warehouse_keeper"})
    public Result reject(@PathVariable Long orderId) {
        boolean result = transferOrderService.rejectTransferOrder(orderId);
        return result ? Result.success("已驳回") : Result.error("驳回失败");
    }

    @Log(value = "完成调拨单", module = "调拨管理")
    @PostMapping("/{orderId}/complete")
    @RequireRole({"admin", "warehouse_keeper"})
    public Result complete(@PathVariable Long orderId) {
        try {
            boolean result = transferOrderService.completeTransferOrder(orderId);
            return result ? Result.success("完成调拨") : Result.error("完成失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Log(value = "删除调拨单", module = "调拨管理")
    @DeleteMapping("/{id}")
    @RequireRole({"admin", "warehouse_keeper"})
    public Result delete(@PathVariable Long id) {
        TransferOrder order = transferOrderService.getById(id);
        if (order != null && order.getStatus() != 0) {
            return Result.error("只能删除待审核的调拨单");
        }
        return transferOrderService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    static class CreateTransferRequest {
        private TransferOrder order;
        private List<TransferItem> items;
        public TransferOrder getOrder() { return order; }
        public void setOrder(TransferOrder order) { this.order = order; }
        public List<TransferItem> getItems() { return items; }
        public void setItems(List<TransferItem> items) { this.items = items; }
    }
}
