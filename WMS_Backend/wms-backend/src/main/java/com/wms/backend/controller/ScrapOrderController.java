package com.wms.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.backend.annotation.Log;
import com.wms.backend.common.Result;
import com.wms.backend.entity.ScrapItem;
import com.wms.backend.entity.ScrapOrder;
import com.wms.backend.service.IScrapItemService;
import com.wms.backend.service.IScrapOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scrap-order")
public class ScrapOrderController {

    @Autowired
    private IScrapOrderService scrapOrderService;

    @Autowired
    private IScrapItemService scrapItemService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(required = false) String scrapType) {
        Page<ScrapOrder> page = new Page<>(current, size);
        QueryWrapper<ScrapOrder> wrapper = new QueryWrapper<>();
        if (status != null) wrapper.eq("status", status);
        if (scrapType != null && !scrapType.isEmpty()) wrapper.eq("scrap_type", scrapType);
        wrapper.orderByDesc("create_time");
        return Result.success(scrapOrderService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(scrapOrderService.getById(id));
    }

    @GetMapping("/{orderId}/items")
    public Result getItems(@PathVariable Long orderId) {
        QueryWrapper<ScrapItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        return Result.success(scrapItemService.list(wrapper));
    }

    @Log(value = "创建报损单", module = "报损管理")
    @PostMapping
    public Result create(@RequestBody CreateScrapRequest request) {
        boolean result = scrapOrderService.createScrapOrder(request.getOrder(), request.getItems());
        return result ? Result.success("创建成功") : Result.error("创建失败");
    }

    @Log(value = "审核报损单", module = "报损管理")
    @PostMapping("/{orderId}/audit")
    public Result audit(@PathVariable Long orderId) {
        boolean result = scrapOrderService.auditScrapOrder(orderId);
        return result ? Result.success("审核成功") : Result.error("审核失败");
    }

    @Log(value = "完成报损单", module = "报损管理")
    @PostMapping("/{orderId}/complete")
    public Result complete(@PathVariable Long orderId) {
        boolean result = scrapOrderService.completeScrapOrder(orderId);
        return result ? Result.success("完成报损") : Result.error("完成失败");
    }

    @Log(value = "删除报损单", module = "报损管理")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        ScrapOrder order = scrapOrderService.getById(id);
        if (order != null && order.getStatus() != 0) {
            return Result.error("只能删除待审核的报损单");
        }
        return scrapOrderService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    static class CreateScrapRequest {
        private ScrapOrder order;
        private List<ScrapItem> items;
        public ScrapOrder getOrder() { return order; }
        public void setOrder(ScrapOrder order) { this.order = order; }
        public List<ScrapItem> getItems() { return items; }
        public void setItems(List<ScrapItem> items) { this.items = items; }
    }
}
