// src/main/java/com/wms/backend/service/impl/OutboundOrderServiceImpl.java
package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.backend.entity.*;
import com.wms.backend.mapper.*;
import com.wms.backend.service.IInventoryService;
import com.wms.backend.service.IOutboundOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OutboundOrderServiceImpl extends ServiceImpl<OutboundOrderMapper, OutboundOrder>  // 修正这里
        implements IOutboundOrderService {

    @Autowired
    private OutboundItemMapper outboundItemMapper;

    @Autowired
    private IInventoryService inventoryService;

    @Autowired
    private InventoryTransactionMapper inventoryTransactionMapper;

    @Override
    @Transactional
    public boolean createOutboundOrder(OutboundOrder order, List<OutboundItem> items) {
        // 1. 计算总金额
        BigDecimal totalAmount = items.stream()
                .map(item -> item.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);

        // 2. 保存出库单
        boolean orderSaved = this.save(order);
        if (!orderSaved) {
            return false;
        }

        // 3. 保存出库明细
        for (OutboundItem item : items) {
            item.setOrderId(order.getId());
            outboundItemMapper.insert(item);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean auditOutboundOrder(Long orderId) {
        OutboundOrder order = this.getById(orderId);
        if (order == null || order.getStatus() != 0) { // 0-待审核
            return false;
        }

        // 检查库存是否充足
        if (!checkStockForOutbound(orderId)) {
            throw new RuntimeException("库存不足，无法审核出库单");
        }

        // 锁定库存
        QueryWrapper<OutboundItem> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("order_id", orderId);
        List<OutboundItem> items = outboundItemMapper.selectList(itemWrapper);
        for (OutboundItem item : items) {
            boolean locked = inventoryService.lockQuantity(
                    order.getWarehouseId(), item.getMaterialId(), item.getQuantity());
            if (!locked) {
                throw new RuntimeException("锁定库存失败，物资ID: " + item.getMaterialId());
            }
        }

        order.setStatus(1); // 1-已审核
        return this.updateById(order);
    }

    @Override
    @Transactional
    public boolean completeOutboundOrder(Long orderId) {
        OutboundOrder order = this.getById(orderId);
        if (order == null || order.getStatus() != 1) { // 1-已审核
            return false;
        }

        // 获取出库明细
        QueryWrapper<OutboundItem> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("order_id", orderId);
        List<OutboundItem> items = outboundItemMapper.selectList(itemWrapper);

        // 扣减库存
        for (OutboundItem item : items) {
            // 先释放锁定
            inventoryService.unlockQuantity(
                    order.getWarehouseId(), item.getMaterialId(), item.getQuantity());

            boolean success = inventoryService.decreaseQuantity(
                    order.getWarehouseId(),
                    item.getMaterialId(),
                    item.getQuantity()
            );

            if (!success) {
                throw new RuntimeException("库存不足，无法完成出库，物资ID: " + item.getMaterialId());
            }

            // 记录库存流水
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setWarehouseId(order.getWarehouseId());
            transaction.setMaterialId(item.getMaterialId());
            transaction.setQuantity(-item.getQuantity()); // 负数表示出库
            transaction.setChangeType("OUT"); // 出库
            transaction.setReferenceType("OUTBOUND");
            transaction.setReferenceId(orderId.toString());
            transaction.setRemark("出库单完成");
            inventoryTransactionMapper.insert(transaction);
        }

        // 更新订单状态为已完成
        order.setStatus(2); // 2-已完成
        return this.updateById(order);
    }

    @Override
    public boolean checkStockForOutbound(Long orderId) {
        OutboundOrder order = this.getById(orderId);
        if (order == null) {
            return false;
        }

        QueryWrapper<OutboundItem> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("order_id", orderId);
        List<OutboundItem> items = outboundItemMapper.selectList(itemWrapper);

        for (OutboundItem item : items) {
            boolean sufficient = inventoryService.checkStockSufficient(
                    order.getWarehouseId(),
                    item.getMaterialId(),
                    item.getQuantity()
            );
            if (!sufficient) {
                return false;
            }
        }

        return true;
    }

    @Override
    @Transactional
    public boolean updateOutboundOrder(Long orderId, OutboundOrder order, List<OutboundItem> items) {
        OutboundOrder existing = this.getById(orderId);
        if (existing == null || existing.getStatus() != 0) {
            throw new RuntimeException("只能编辑待审核的出库单");
        }

        order.setId(orderId);
        BigDecimal totalAmount = items.stream()
                .map(OutboundItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        this.updateById(order);

        QueryWrapper<OutboundItem> delWrapper = new QueryWrapper<>();
        delWrapper.eq("order_id", orderId);
        outboundItemMapper.delete(delWrapper);

        for (OutboundItem item : items) {
            item.setId(null);
            item.setOrderId(orderId);
            outboundItemMapper.insert(item);
        }

        return true;
    }
}