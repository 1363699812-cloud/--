package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.backend.entity.*;
import com.wms.backend.mapper.StocktakeItemMapper;
import com.wms.backend.mapper.StocktakeOrderMapper;
import com.wms.backend.mapper.InventoryTransactionMapper;
import com.wms.backend.service.IInventoryService;
import com.wms.backend.service.IStocktakeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StocktakeOrderServiceImpl extends ServiceImpl<StocktakeOrderMapper, StocktakeOrder>
        implements IStocktakeOrderService {

    @Autowired
    private StocktakeItemMapper stocktakeItemMapper;

    @Autowired
    private IInventoryService inventoryService;

    @Autowired
    private InventoryTransactionMapper inventoryTransactionMapper;

    @Override
    @Transactional
    public boolean createStocktakeOrder(StocktakeOrder order, List<StocktakeItem> items) {
        order.setStatus(0);
        boolean saved = this.save(order);
        if (!saved) return false;

        for (StocktakeItem item : items) {
            item.setOrderId(order.getId());
            // 自动填充系统数量
            Inventory inventory = inventoryService.getByWarehouseAndMaterial(
                    order.getWarehouseId(), item.getMaterialId());
            item.setSystemQuantity(inventory != null ? inventory.getQuantity() : 0);
            // 差异 = 实盘 - 系统
            if (item.getActualQuantity() != null) {
                item.setDiffQuantity(item.getActualQuantity() - item.getSystemQuantity());
            }
            stocktakeItemMapper.insert(item);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean completeStocktakeOrder(Long orderId) {
        StocktakeOrder order = this.getById(orderId);
        if (order == null || order.getStatus() != 0) return false;

        QueryWrapper<StocktakeItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        List<StocktakeItem> items = stocktakeItemMapper.selectList(wrapper);

        for (StocktakeItem item : items) {
            if (item.getDiffQuantity() == null || item.getDiffQuantity() == 0) {
                continue; // 无差异，跳过
            }

            if (item.getDiffQuantity() > 0) {
                // 盘盈：实际多于系统，增加库存
                inventoryService.increaseQuantity(
                        order.getWarehouseId(), item.getMaterialId(), item.getDiffQuantity());
            } else {
                // 盘亏：实际少于系统，减少库存
                inventoryService.decreaseQuantity(
                        order.getWarehouseId(), item.getMaterialId(), Math.abs(item.getDiffQuantity()));
            }

            // 记录库存流水
            InventoryTransaction tx = new InventoryTransaction();
            tx.setWarehouseId(order.getWarehouseId());
            tx.setMaterialId(item.getMaterialId());
            tx.setQuantity(item.getDiffQuantity());
            tx.setChangeType("STOCKTAKE");
            tx.setReferenceType("STOCKTAKE");
            tx.setReferenceId(orderId.toString());
            tx.setRemark("盘点调整：系统" + item.getSystemQuantity() + "，实盘" + item.getActualQuantity());
            inventoryTransactionMapper.insert(tx);
        }

        order.setStatus(1);
        return this.updateById(order);
    }
}
