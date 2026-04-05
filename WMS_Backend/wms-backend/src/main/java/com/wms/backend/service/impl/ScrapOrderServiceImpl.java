package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.backend.entity.*;
import com.wms.backend.mapper.ScrapItemMapper;
import com.wms.backend.mapper.ScrapOrderMapper;
import com.wms.backend.mapper.InventoryTransactionMapper;
import com.wms.backend.service.IInventoryService;
import com.wms.backend.service.IScrapOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wms.backend.utils.OrderNoUtil;
import java.util.List;

@Service
public class ScrapOrderServiceImpl extends ServiceImpl<ScrapOrderMapper, ScrapOrder>
        implements IScrapOrderService {

    @Autowired
    private ScrapItemMapper scrapItemMapper;

    @Autowired
    private IInventoryService inventoryService;

    @Autowired
    private InventoryTransactionMapper inventoryTransactionMapper;

    @Override
    @Transactional
    public boolean createScrapOrder(ScrapOrder order, List<ScrapItem> items) {
        order.setOrderNo(OrderNoUtil.generate("BS"));
        order.setStatus(0);
        boolean saved = this.save(order);
        if (!saved) return false;

        for (ScrapItem item : items) {
            item.setOrderId(order.getId());
            scrapItemMapper.insert(item);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean auditScrapOrder(Long orderId) {
        ScrapOrder order = this.getById(orderId);
        if (order == null || order.getStatus() != 0) return false;

        // 检查库存是否充足
        QueryWrapper<ScrapItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        List<ScrapItem> items = scrapItemMapper.selectList(wrapper);
        for (ScrapItem item : items) {
            if (!inventoryService.checkStockSufficient(order.getWarehouseId(), item.getMaterialId(), item.getQuantity())) {
                throw new RuntimeException("库存不足，无法审核报损单，物资ID: " + item.getMaterialId());
            }
        }

        order.setStatus(1);
        return this.updateById(order);
    }

    @Override
    @Transactional
    public boolean rejectScrapOrder(Long orderId) {
        ScrapOrder order = this.getById(orderId);
        if (order == null || order.getStatus() != 0) return false;
        order.setStatus(-1);
        return this.updateById(order);
    }

    @Override
    @Transactional
    public boolean completeScrapOrder(Long orderId) {
        ScrapOrder order = this.getById(orderId);
        if (order == null || order.getStatus() != 1) return false;

        QueryWrapper<ScrapItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        List<ScrapItem> items = scrapItemMapper.selectList(wrapper);

        for (ScrapItem item : items) {
            boolean decreased = inventoryService.decreaseQuantity(
                    order.getWarehouseId(), item.getMaterialId(), item.getQuantity());
            if (!decreased) {
                throw new RuntimeException("报损失败：库存不足，物资ID: " + item.getMaterialId());
            }

            InventoryTransaction tx = new InventoryTransaction();
            tx.setWarehouseId(order.getWarehouseId());
            tx.setMaterialId(item.getMaterialId());
            tx.setQuantity(-item.getQuantity());
            tx.setChangeType(order.getScrapType()); // DAMAGE 或 SCRAP
            tx.setReferenceType("SCRAP");
            tx.setReferenceId(orderId.toString());
            tx.setRemark("报损报废: " + order.getReason());
            inventoryTransactionMapper.insert(tx);
        }

        order.setStatus(2);
        return this.updateById(order);
    }
}
