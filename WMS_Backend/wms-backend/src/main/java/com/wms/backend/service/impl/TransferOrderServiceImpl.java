package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.backend.entity.*;
import com.wms.backend.mapper.TransferItemMapper;
import com.wms.backend.mapper.TransferOrderMapper;
import com.wms.backend.mapper.InventoryTransactionMapper;
import com.wms.backend.service.IInventoryService;
import com.wms.backend.service.ITransferOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransferOrderServiceImpl extends ServiceImpl<TransferOrderMapper, TransferOrder>
        implements ITransferOrderService {

    @Autowired
    private TransferItemMapper transferItemMapper;

    @Autowired
    private IInventoryService inventoryService;

    @Autowired
    private InventoryTransactionMapper inventoryTransactionMapper;

    @Override
    @Transactional
    public boolean createTransferOrder(TransferOrder order, List<TransferItem> items) {
        order.setStatus(0);
        boolean saved = this.save(order);
        if (!saved) return false;

        for (TransferItem item : items) {
            item.setOrderId(order.getId());
            transferItemMapper.insert(item);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean auditTransferOrder(Long orderId) {
        TransferOrder order = this.getById(orderId);
        if (order == null || order.getStatus() != 0) return false;

        // 检查源仓库库存是否充足
        QueryWrapper<TransferItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        List<TransferItem> items = transferItemMapper.selectList(wrapper);

        for (TransferItem item : items) {
            if (!inventoryService.checkStockSufficient(order.getFromWarehouseId(), item.getMaterialId(), item.getQuantity())) {
                throw new RuntimeException("源仓库库存不足，物资ID: " + item.getMaterialId());
            }
        }

        order.setStatus(1);
        return this.updateById(order);
    }

    @Override
    @Transactional
    public boolean completeTransferOrder(Long orderId) {
        TransferOrder order = this.getById(orderId);
        if (order == null || order.getStatus() != 1) return false;

        QueryWrapper<TransferItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        List<TransferItem> items = transferItemMapper.selectList(wrapper);

        for (TransferItem item : items) {
            // 源仓库减库存
            boolean decreased = inventoryService.decreaseQuantity(
                    order.getFromWarehouseId(), item.getMaterialId(), item.getQuantity());
            if (!decreased) {
                throw new RuntimeException("调拨失败：源仓库库存不足，物资ID: " + item.getMaterialId());
            }

            // 目标仓库加库存
            inventoryService.increaseQuantity(
                    order.getToWarehouseId(), item.getMaterialId(), item.getQuantity());

            // 记录调出流水
            InventoryTransaction outTx = new InventoryTransaction();
            outTx.setWarehouseId(order.getFromWarehouseId());
            outTx.setMaterialId(item.getMaterialId());
            outTx.setQuantity(-item.getQuantity());
            outTx.setChangeType("TRANSFER_OUT");
            outTx.setReferenceType("TRANSFER");
            outTx.setReferenceId(orderId.toString());
            outTx.setRemark("调拨出库");
            inventoryTransactionMapper.insert(outTx);

            // 记录调入流水
            InventoryTransaction inTx = new InventoryTransaction();
            inTx.setWarehouseId(order.getToWarehouseId());
            inTx.setMaterialId(item.getMaterialId());
            inTx.setQuantity(item.getQuantity());
            inTx.setChangeType("TRANSFER_IN");
            inTx.setReferenceType("TRANSFER");
            inTx.setReferenceId(orderId.toString());
            inTx.setRemark("调拨入库");
            inventoryTransactionMapper.insert(inTx);
        }

        order.setStatus(2);
        return this.updateById(order);
    }
}
