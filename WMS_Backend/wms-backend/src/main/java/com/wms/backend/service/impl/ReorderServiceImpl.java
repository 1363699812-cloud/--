package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wms.backend.entity.*;
import com.wms.backend.mapper.*;
import com.wms.backend.service.IReorderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ReorderServiceImpl implements IReorderService {

    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private MaterialMapper materialMapper;
    @Autowired
    private OutboundItemMapper outboundItemMapper;
    @Autowired
    private OutboundOrderMapper outboundOrderMapper;
    @Autowired
    private WarehouseMapper warehouseMapper;
    @Override
    public List<Map<String, Object>> getReorderSuggestions() {
        // 先重算所有物资的ROP和EOQ
        recalculateReorderParams();

        List<Map<String, Object>> result = new ArrayList<>();

        // 查询所有库存记录
        List<Inventory> inventories = inventoryMapper.selectList(null);

        for (Inventory inv : inventories) {
            Material material = materialMapper.selectById(inv.getMaterialId());
            if (material == null || material.getStatus() == 0) continue;

            int currentQty = inv.getQuantity() != null ? inv.getQuantity() : 0;
            int safetyStock = material.getSafetyStock() != null ? material.getSafetyStock() : 0;
            int reorderPoint = material.getReorderPoint() != null ? material.getReorderPoint() : safetyStock;
            int eoq = material.getEoq() != null ? material.getEoq() : 0;

            // 补货建议: 当前库存 <= 补货点
            if (currentQty <= reorderPoint) {
                Warehouse warehouse = warehouseMapper.selectById(inv.getWarehouseId());

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("warehouseId", inv.getWarehouseId());
                item.put("warehouseName", warehouse != null ? warehouse.getName() : "");
                item.put("materialId", material.getId());
                item.put("materialCode", material.getCode());
                item.put("materialName", material.getName());
                item.put("unit", material.getUnit());
                item.put("currentStock", currentQty);
                item.put("safetyStock", safetyStock);
                item.put("reorderPoint", reorderPoint);
                item.put("eoq", eoq);
                // 建议补货量 = max(EOQ, 补货点 + 安全库存 - 当前库存)
                int suggestedQty = Math.max(eoq, reorderPoint - currentQty + safetyStock);
                item.put("suggestedQuantity", suggestedQty);
                item.put("shortage", Math.max(0, reorderPoint - currentQty));
                item.put("maxStock", material.getMaxStock());

                // 紧急程度
                String urgency;
                if (currentQty <= 0) {
                    urgency = "严重缺货";
                } else if (currentQty <= safetyStock) {
                    urgency = "紧急补货";
                } else if (currentQty <= reorderPoint) {
                    urgency = "建议补货";
                } else {
                    urgency = "正常";
                }
                item.put("urgency", urgency);

                result.add(item);
            }
        }

        // 按紧急程度排序：缺货优先
        result.sort((a, b) -> {
            int qtyA = (int) a.get("currentStock");
            int qtyB = (int) b.get("currentStock");
            return Integer.compare(qtyA, qtyB);
        });

        return result;
    }

    @Override
    @Transactional
    public void recalculateReorderParams() {
        List<Material> materials = materialMapper.selectList(null);

        for (Material material : materials) {
            // 计算日均需求量（最近90天出库总量 / 90）
            Double dailyDemand = calculateDailyDemand(material.getId());
            // 提前期默认7天，如果已设置则使用设置值
            int leadTime = material.getLeadTime() != null && material.getLeadTime() > 0
                    ? material.getLeadTime() : 7;
            int safetyStock = material.getSafetyStock() != null ? material.getSafetyStock() : 0;

            // ROP = d̄ × L + SS
            int reorderPoint = (int) Math.ceil(dailyDemand * leadTime) + safetyStock;

            // EOQ = sqrt(2DS/H)
            // D = 年需求量 = dailyDemand * 365
            // S = orderCost (单次订货成本)
            // H = holdingCost (单位年持有成本)
            int eoq = 0;
            BigDecimal orderCost = material.getOrderCost();
            BigDecimal holdingCost = material.getHoldingCost();
            if (orderCost != null && holdingCost != null
                    && holdingCost.compareTo(BigDecimal.ZERO) > 0 && dailyDemand > 0) {
                double annualDemand = dailyDemand * 365;
                double s = orderCost.doubleValue();
                double h = holdingCost.doubleValue();
                eoq = (int) Math.round(Math.sqrt(2 * annualDemand * s / h));
            } else if (safetyStock > 0) {
                // 如果没有设置成本参数，默认 EOQ = 安全库存 × 2
                eoq = safetyStock * 2;
            }

            // 更新material表
            Material update = new Material();
            update.setId(material.getId());
            update.setDailyDemand(BigDecimal.valueOf(dailyDemand).setScale(2, RoundingMode.HALF_UP));
            update.setReorderPoint(reorderPoint);
            update.setEoq(eoq);
            materialMapper.updateById(update);
        }
    }

    private Double calculateDailyDemand(Long materialId) {
        // 统计最近90天已完成出库单中的该物资总量
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(90);

        QueryWrapper<OutboundOrder> orderQw = new QueryWrapper<>();
        orderQw.select("id")
                .eq("status", 2) // 已完成
                .ge("create_time", LocalDateTime.of(startDate, LocalTime.MIN))
                .le("create_time", LocalDateTime.of(endDate, LocalTime.MAX));
        List<OutboundOrder> orders = outboundOrderMapper.selectList(orderQw);

        if (orders.isEmpty()) {
            return 0.0;
        }

        int totalOutQty = 0;
        for (OutboundOrder order : orders) {
            QueryWrapper<OutboundItem> itemQw = new QueryWrapper<>();
            itemQw.select("IFNULL(SUM(quantity), 0) as qty")
                    .eq("order_id", order.getId())
                    .eq("material_id", materialId);
            List<Map<String, Object>> qtyRes = outboundItemMapper.selectMaps(itemQw);
            if (!qtyRes.isEmpty() && qtyRes.get(0).get("qty") != null) {
                totalOutQty += ((Number) qtyRes.get(0).get("qty")).intValue();
            }
        }

        return totalOutQty / 90.0;
    }
}
