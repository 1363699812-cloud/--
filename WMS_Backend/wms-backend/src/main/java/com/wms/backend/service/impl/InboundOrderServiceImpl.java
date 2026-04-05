// src/main/java/com/wms/backend/service/impl/InboundOrderServiceImpl.java
package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.backend.entity.*;
import com.wms.backend.mapper.*;
import com.wms.backend.service.IInboundOrderService;
import com.wms.backend.service.IInventoryService;
import com.wms.backend.service.IWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InboundOrderServiceImpl extends ServiceImpl<InboundOrderMapper, InboundOrder>
        implements IInboundOrderService {

    @Autowired
    private InboundItemMapper inboundItemMapper;

    @Autowired
    private IInventoryService inventoryService;

    @Autowired
    private InventoryTransactionMapper inventoryTransactionMapper;

    @Autowired
    private IWarehouseService warehouseService;

    @Override
    @Transactional
    public boolean createInboundOrder(InboundOrder order, List<InboundItem> items) {
        // 1. 计算总金额
        BigDecimal totalAmount = items.stream()
                .map(item -> item.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);

        // 2. 保存入库单
        boolean orderSaved = this.save(order);
        if (!orderSaved) {
            return false;
        }

        // 3. 保存入库明细
        for (InboundItem item : items) {
            item.setOrderId(order.getId());
            inboundItemMapper.insert(item);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean auditInboundOrder(Long orderId) {
        InboundOrder order = this.getById(orderId);
        if (order == null || order.getStatus() != 0) { // 0-待审核
            return false;
        }

        order.setStatus(1); // 1-已审核
        return this.updateById(order);
    }

    @Override
    @Transactional
    public boolean completeInboundOrder(Long orderId) {
        InboundOrder order = this.getById(orderId);
        if (order == null || order.getStatus() != 1) { // 1-已审核
            return false;
        }

        // 获取入库明细
        QueryWrapper<InboundItem> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("order_id", orderId);
        List<InboundItem> items = inboundItemMapper.selectList(itemWrapper);

        // 校验仓库容量
        Warehouse warehouse = warehouseService.getById(order.getWarehouseId());
        if (warehouse != null && warehouse.getCapacity() != null && warehouse.getCapacity() > 0) {
            Integer currentTotal = inventoryService.getTotalQuantityByWarehouseId(order.getWarehouseId());
            int inboundTotal = items.stream().mapToInt(InboundItem::getQuantity).sum();
            if (currentTotal + inboundTotal > warehouse.getCapacity()) {
                throw new RuntimeException("仓库容量不足，当前库存: " + currentTotal
                        + "，入库数量: " + inboundTotal + "，仓库容量: " + warehouse.getCapacity());
            }
        }

        // 更新库存
        for (InboundItem item : items) {
            boolean success = inventoryService.increaseQuantity(
                    order.getWarehouseId(),
                    item.getMaterialId(),
                    item.getQuantity()
            );

            if (!success) {
                throw new RuntimeException("更新库存失败，物资ID: " + item.getMaterialId());
            }

            // 记录库存流水
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setWarehouseId(order.getWarehouseId());
            transaction.setMaterialId(item.getMaterialId());
            transaction.setQuantity(item.getQuantity());
            transaction.setChangeType("IN"); // 入库
            transaction.setReferenceType("INBOUND");
            transaction.setReferenceId(orderId.toString());
            transaction.setRemark("入库单完成");
            inventoryTransactionMapper.insert(transaction);
        }

        // 更新订单状态为已完成
        order.setStatus(2); // 2-已完成
        return this.updateById(order);
    }

    @Override
    @Transactional
    public boolean updateInboundOrder(Long orderId, InboundOrder order, List<InboundItem> items) {
        InboundOrder existing = this.getById(orderId);
        if (existing == null || existing.getStatus() != 0) {
            throw new RuntimeException("只能编辑待审核的入库单");
        }

        // 更新主表
        order.setId(orderId);
        BigDecimal totalAmount = items.stream()
                .map(InboundItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        this.updateById(order);

        // 删除旧明细
        QueryWrapper<InboundItem> delWrapper = new QueryWrapper<>();
        delWrapper.eq("order_id", orderId);
        inboundItemMapper.delete(delWrapper);

        // 插入新明细
        for (InboundItem item : items) {
            item.setId(null);
            item.setOrderId(orderId);
            inboundItemMapper.insert(item);
        }

        return true;
    }
}