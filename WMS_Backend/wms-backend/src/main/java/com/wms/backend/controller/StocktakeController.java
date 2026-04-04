package com.wms.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.backend.annotation.Log;
import com.wms.backend.common.Result;
import com.wms.backend.entity.StocktakeItem;
import com.wms.backend.entity.StocktakeOrder;
import com.wms.backend.service.IStocktakeItemService;
import com.wms.backend.service.IStocktakeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocktake")
public class StocktakeController {

    @Autowired
    private IStocktakeOrderService stocktakeOrderService;

    @Autowired
    private IStocktakeItemService stocktakeItemService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) Integer status) {
        Page<StocktakeOrder> page = new Page<>(current, size);
        QueryWrapper<StocktakeOrder> wrapper = new QueryWrapper<>();
        if (status != null) wrapper.eq("status", status);
        wrapper.orderByDesc("create_time");
        return Result.success(stocktakeOrderService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(stocktakeOrderService.getById(id));
    }

    @GetMapping("/{orderId}/items")
    public Result getItems(@PathVariable Long orderId) {
        QueryWrapper<StocktakeItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        return Result.success(stocktakeItemService.list(wrapper));
    }

    @Log(value = "创建盘点单", module = "盘点管理")
    @PostMapping
    public Result create(@RequestBody CreateStocktakeRequest request) {
        boolean result = stocktakeOrderService.createStocktakeOrder(request.getOrder(), request.getItems());
        return result ? Result.success("创建成功") : Result.error("创建失败");
    }

    @Log(value = "完成盘点", module = "盘点管理")
    @PostMapping("/{orderId}/complete")
    public Result complete(@PathVariable Long orderId) {
        boolean result = stocktakeOrderService.completeStocktakeOrder(orderId);
        return result ? Result.success("盘点完成") : Result.error("完成失败");
    }

    /**
     * 更新盘点明细（录入实盘数量）
     */
    @PutMapping("/item/{itemId}")
    public Result updateItem(@PathVariable Long itemId, @RequestBody StocktakeItem item) {
        item.setId(itemId);
        if (item.getActualQuantity() != null && item.getSystemQuantity() != null) {
            item.setDiffQuantity(item.getActualQuantity() - item.getSystemQuantity());
        }
        return stocktakeItemService.updateById(item) ? Result.success("更新成功") : Result.error("更新失败");
    }

    @Log(value = "删除盘点单", module = "盘点管理")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        StocktakeOrder order = stocktakeOrderService.getById(id);
        if (order != null && order.getStatus() != 0) {
            return Result.error("只能删除进行中的盘点单");
        }
        return stocktakeOrderService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    static class CreateStocktakeRequest {
        private StocktakeOrder order;
        private List<StocktakeItem> items;
        public StocktakeOrder getOrder() { return order; }
        public void setOrder(StocktakeOrder order) { this.order = order; }
        public List<StocktakeItem> getItems() { return items; }
        public void setItems(List<StocktakeItem> items) { this.items = items; }
    }
}
