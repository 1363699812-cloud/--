package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wms.backend.entity.*;
import com.wms.backend.mapper.*;
import com.wms.backend.service.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ReportServiceImpl implements IReportService {

    @Autowired
    private MaterialMapper materialMapper;
    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private InboundOrderMapper inboundOrderMapper;
    @Autowired
    private OutboundOrderMapper outboundOrderMapper;
    @Autowired
    private WarehouseMapper warehouseMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private InventoryTransactionMapper inventoryTransactionMapper;
    @Autowired
    private InboundItemMapper inboundItemMapper;
    @Autowired
    private OutboundItemMapper outboundItemMapper;

    @Override
    public Map<String, Object> getDashboard() {
        Map<String, Object> result = new HashMap<>();

        // 物资总数
        result.put("materialCount", materialMapper.selectCount(null));

        // 仓库总数
        result.put("warehouseCount", warehouseMapper.selectCount(null));

        // 当前库存总量
        QueryWrapper<Inventory> invQw = new QueryWrapper<>();
        invQw.select("IFNULL(SUM(quantity),0) as total");
        List<Map<String, Object>> invResult = inventoryMapper.selectMaps(invQw);
        result.put("totalInventory", invResult.isEmpty() ? 0 : invResult.get(0).get("total"));

        // 今日入库单数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Long todayInboundCount = inboundOrderMapper.selectCount(
                new QueryWrapper<InboundOrder>().between("create_time", todayStart, todayEnd));
        result.put("todayInboundCount", todayInboundCount);
        result.put("todayInbound", todayInboundCount);

        // 今日出库单数
        Long todayOutboundCount = outboundOrderMapper.selectCount(
                new QueryWrapper<OutboundOrder>().between("create_time", todayStart, todayEnd));
        result.put("todayOutboundCount", todayOutboundCount);
        result.put("todayOutbound", todayOutboundCount);

        // 库存预警数量
        List<Map<String, Object>> alerts = inventoryMapper.selectStockAlerts();
        result.put("alertCount", alerts.size());

        // 待审核入库单
        result.put("pendingInbound", inboundOrderMapper.selectCount(
                new QueryWrapper<InboundOrder>().eq("status", 0)));

        // 待审核出库单
        result.put("pendingOutbound", outboundOrderMapper.selectCount(
                new QueryWrapper<OutboundOrder>().eq("status", 0)));

        // 最近7天入库趋势
        List<Map<String, Object>> inboundTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            Long count = inboundOrderMapper.selectCount(
                    new QueryWrapper<InboundOrder>()
                            .between("create_time",
                                    LocalDateTime.of(date, LocalTime.MIN),
                                    LocalDateTime.of(date, LocalTime.MAX)));
            item.put("count", count);
            inboundTrend.add(item);
        }
        result.put("inboundTrend", inboundTrend);

        // 最近7天出库趋势
        List<Map<String, Object>> outboundTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            Long count = outboundOrderMapper.selectCount(
                    new QueryWrapper<OutboundOrder>()
                            .between("create_time",
                                    LocalDateTime.of(date, LocalTime.MIN),
                                    LocalDateTime.of(date, LocalTime.MAX)));
            item.put("count", count);
            outboundTrend.add(item);
        }
        result.put("outboundTrend", outboundTrend);

        // 组合趋势数据（前端读取 trends 数组）
        List<Map<String, Object>> trends = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Map<String, Object> trendItem = new HashMap<>();
            trendItem.put("date", date.toString());
            Long inCount = inboundOrderMapper.selectCount(
                    new QueryWrapper<InboundOrder>()
                            .between("create_time",
                                    LocalDateTime.of(date, LocalTime.MIN),
                                    LocalDateTime.of(date, LocalTime.MAX)));
            Long outCount = outboundOrderMapper.selectCount(
                    new QueryWrapper<OutboundOrder>()
                            .between("create_time",
                                    LocalDateTime.of(date, LocalTime.MIN),
                                    LocalDateTime.of(date, LocalTime.MAX)));
            trendItem.put("inbound", inCount);
            trendItem.put("outbound", outCount);
            trends.add(trendItem);
        }
        result.put("trends", trends);

        return result;
    }

    @Override
    public List<Map<String, Object>> getInventorySummary(Long warehouseId) {
        QueryWrapper<Inventory> qw = new QueryWrapper<>();
        qw.select("i.warehouse_id AS warehouseId", "i.material_id AS materialId",
                   "i.quantity", "i.lock_quantity AS lockQuantity",
                   "m.name AS materialName", "m.code AS materialCode", "m.unit",
                   "w.name AS warehouseName")
           .eq(warehouseId != null, "i.warehouse_id", warehouseId)
           .last("FROM inventory i LEFT JOIN material m ON i.material_id = m.id LEFT JOIN warehouse w ON i.warehouse_id = w.id");

        // 使用自定义SQL替代，因为QueryWrapper不支持多表JOIN
        return inventoryMapper.selectMaps(
                new QueryWrapper<Inventory>()
                        .apply(warehouseId != null, "warehouse_id = {0}", warehouseId)
                        .orderByDesc("quantity"));
    }

    @Override
    public List<Map<String, Object>> getInboundStats(String startDate, String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            LocalDateTime dayStart = LocalDateTime.of(current, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(current, LocalTime.MAX);

            QueryWrapper<InboundOrder> countQw = new QueryWrapper<>();
            countQw.between("create_time", dayStart, dayEnd);
            Long orderCount = inboundOrderMapper.selectCount(countQw);

            QueryWrapper<InboundOrder> amountQw = new QueryWrapper<>();
            amountQw.select("IFNULL(SUM(total_amount),0) as totalAmount");
            amountQw.between("create_time", dayStart, dayEnd);
            List<Map<String, Object>> amountRes = inboundOrderMapper.selectMaps(amountQw);
            Object totalAmount = amountRes.isEmpty() ? 0 : amountRes.get(0).get("totalAmount");

            // 获取当日入库总数量
            QueryWrapper<InboundOrder> orderQw = new QueryWrapper<>();
            orderQw.select("id").between("create_time", dayStart, dayEnd);
            List<InboundOrder> orders = inboundOrderMapper.selectList(orderQw);
            int totalQuantity = 0;
            for (InboundOrder order : orders) {
                QueryWrapper<InboundItem> itemQw = new QueryWrapper<>();
                itemQw.select("IFNULL(SUM(quantity),0) as qty").eq("order_id", order.getId());
                List<Map<String, Object>> qtyRes = inboundItemMapper.selectMaps(itemQw);
                if (!qtyRes.isEmpty() && qtyRes.get(0).get("qty") != null) {
                    totalQuantity += ((Number) qtyRes.get(0).get("qty")).intValue();
                }
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", current.toString());
            item.put("orderCount", orderCount);
            item.put("totalQuantity", totalQuantity);
            item.put("totalAmount", totalAmount);
            result.add(item);

            current = current.plusDays(1);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getOutboundStats(String startDate, String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            LocalDateTime dayStart = LocalDateTime.of(current, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(current, LocalTime.MAX);

            QueryWrapper<OutboundOrder> countQw = new QueryWrapper<>();
            countQw.between("create_time", dayStart, dayEnd);
            Long orderCount = outboundOrderMapper.selectCount(countQw);

            QueryWrapper<OutboundOrder> amountQw = new QueryWrapper<>();
            amountQw.select("IFNULL(SUM(total_amount),0) as totalAmount");
            amountQw.between("create_time", dayStart, dayEnd);
            List<Map<String, Object>> amountRes = outboundOrderMapper.selectMaps(amountQw);
            Object totalAmount = amountRes.isEmpty() ? 0 : amountRes.get(0).get("totalAmount");

            QueryWrapper<OutboundOrder> orderQw = new QueryWrapper<>();
            orderQw.select("id").between("create_time", dayStart, dayEnd);
            List<OutboundOrder> orders = outboundOrderMapper.selectList(orderQw);
            int totalQuantity = 0;
            for (OutboundOrder order : orders) {
                QueryWrapper<OutboundItem> itemQw = new QueryWrapper<>();
                itemQw.select("IFNULL(SUM(quantity),0) as qty").eq("order_id", order.getId());
                List<Map<String, Object>> qtyRes = outboundItemMapper.selectMaps(itemQw);
                if (!qtyRes.isEmpty() && qtyRes.get(0).get("qty") != null) {
                    totalQuantity += ((Number) qtyRes.get(0).get("qty")).intValue();
                }
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", current.toString());
            item.put("orderCount", orderCount);
            item.put("totalQuantity", totalQuantity);
            item.put("totalAmount", totalAmount);
            result.add(item);

            current = current.plusDays(1);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getCategoryStats() {
        QueryWrapper<Material> qw = new QueryWrapper<>();
        qw.select("category_id AS categoryId", "COUNT(*) AS materialCount")
           .groupBy("category_id");
        List<Map<String, Object>> stats = materialMapper.selectMaps(qw);

        // 按分类汇总库存总量：先获取所有物资的分类映射，再按分类聚合库存
        Map<Long, Long> categoryQuantityMap = new HashMap<>();
        List<Material> allMaterials = materialMapper.selectList(null);
        Map<Long, Long> materialCategoryMap = new HashMap<>();
        for (Material m : allMaterials) {
            materialCategoryMap.put(m.getId(), m.getCategoryId());
        }
        List<Inventory> allInventory = inventoryMapper.selectList(null);
        for (Inventory inv : allInventory) {
            Long catId = materialCategoryMap.get(inv.getMaterialId());
            if (catId != null) {
                categoryQuantityMap.merge(catId, (long) inv.getQuantity(), Long::sum);
            }
        }

        // 补充类别名称和库存总量
        for (Map<String, Object> stat : stats) {
            Object categoryId = stat.get("categoryId");
            if (categoryId != null) {
                Long catId = ((Number) categoryId).longValue();
                Category category = categoryMapper.selectById(catId);
                stat.put("categoryName", category != null ? category.getName() : "未分类");
                stat.put("totalQuantity", categoryQuantityMap.getOrDefault(catId, 0L));
            } else {
                stat.put("categoryName", "未分类");
                stat.put("totalQuantity", 0);
            }
        }
        return stats;
    }

    @Override
    public List<Map<String, Object>> getAbcAnalysis() {
        // 1. 获取所有库存记录
        List<Inventory> inventories = inventoryMapper.selectList(null);

        // 2. 获取每个物资的最近入库均价
        Map<Long, BigDecimal> avgPriceMap = new HashMap<>();
        QueryWrapper<InboundItem> priceQw = new QueryWrapper<>();
        priceQw.select("material_id AS materialId",
                "AVG(unit_price) AS avgPrice")
                .groupBy("material_id");
        List<Map<String, Object>> priceResult = inboundItemMapper.selectMaps(priceQw);
        for (Map<String, Object> row : priceResult) {
            Long materialId = ((Number) row.get("materialId")).longValue();
            BigDecimal avgPrice = row.get("avgPrice") instanceof BigDecimal
                    ? (BigDecimal) row.get("avgPrice")
                    : new BigDecimal(row.get("avgPrice").toString());
            avgPriceMap.put(materialId, avgPrice);
        }

        // 3. 按物资汇总库存价值
        Map<Long, BigDecimal> valueMap = new HashMap<>();
        Map<Long, Integer> qtyMap = new HashMap<>();
        for (Inventory inv : inventories) {
            BigDecimal price = avgPriceMap.getOrDefault(inv.getMaterialId(), BigDecimal.ONE);
            BigDecimal value = price.multiply(BigDecimal.valueOf(inv.getQuantity()));
            valueMap.merge(inv.getMaterialId(), value, BigDecimal::add);
            qtyMap.merge(inv.getMaterialId(), inv.getQuantity(), Integer::sum);
        }

        // 4. 按价值降序排序
        List<Map.Entry<Long, BigDecimal>> sorted = new ArrayList<>(valueMap.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        BigDecimal totalValue = sorted.stream()
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. ABC分类 (A:80%, B:15%, C:5%)
        List<Map<String, Object>> result = new ArrayList<>();
        BigDecimal cumValue = BigDecimal.ZERO;

        for (Map.Entry<Long, BigDecimal> entry : sorted) {
            Long materialId = entry.getKey();
            BigDecimal value = entry.getValue();
            cumValue = cumValue.add(value);

            double cumPercent = totalValue.compareTo(BigDecimal.ZERO) == 0 ? 0
                    : cumValue.divide(totalValue, 4, RoundingMode.HALF_UP).doubleValue() * 100;

            String classification;
            if (cumPercent <= 80) {
                classification = "A";
            } else if (cumPercent <= 95) {
                classification = "B";
            } else {
                classification = "C";
            }

            Material material = materialMapper.selectById(materialId);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("materialId", materialId);
            item.put("materialCode", material != null ? material.getCode() : "");
            item.put("materialName", material != null ? material.getName() : "未知物资");
            item.put("quantity", qtyMap.getOrDefault(materialId, 0));
            item.put("avgPrice", avgPriceMap.getOrDefault(materialId, BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP));
            item.put("totalValue", value.setScale(2, RoundingMode.HALF_UP));
            item.put("valuePercent", totalValue.compareTo(BigDecimal.ZERO) == 0 ? 0
                    : value.divide(totalValue, 4, RoundingMode.HALF_UP).doubleValue() * 100);
            item.put("cumPercent", cumPercent);
            item.put("classification", classification);
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getTurnoverRate(String startDate, String endDate) {
        // 默认最近30天
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : end.minusDays(30);
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;

        // 1. 统计期间出库数量 (按物资)
        QueryWrapper<OutboundItem> outQw = new QueryWrapper<>();
        outQw.select("material_id AS materialId", "SUM(quantity) AS outQty")
                .inSql("order_id",
                        "SELECT id FROM outbound_order WHERE status = 2 AND create_time >= '"
                                + start + " 00:00:00' AND create_time <= '" + end + " 23:59:59'")
                .groupBy("material_id");
        List<Map<String, Object>> outResult = outboundItemMapper.selectMaps(outQw);

        Map<Long, Integer> outQtyMap = new HashMap<>();
        for (Map<String, Object> row : outResult) {
            Long materialId = ((Number) row.get("materialId")).longValue();
            Integer qty = ((Number) row.get("outQty")).intValue();
            outQtyMap.put(materialId, qty);
        }

        // 2. 获取当前库存
        List<Inventory> inventories = inventoryMapper.selectList(null);
        Map<Long, Integer> currentQtyMap = new HashMap<>();
        for (Inventory inv : inventories) {
            currentQtyMap.merge(inv.getMaterialId(), inv.getQuantity(), Integer::sum);
        }

        // 3. 计算周转率 = 出库量 / 平均库存 * (365/天数)
        Set<Long> allMaterials = new HashSet<>();
        allMaterials.addAll(outQtyMap.keySet());
        allMaterials.addAll(currentQtyMap.keySet());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Long materialId : allMaterials) {
            int outQty = outQtyMap.getOrDefault(materialId, 0);
            int currentQty = currentQtyMap.getOrDefault(materialId, 0);
            // 近似平均库存 = (当前库存 + 出库量) / 2
            double avgInventory = Math.max((currentQty + outQty) / 2.0, 1);
            double turnoverRate = (outQty / avgInventory) * (365.0 / days);
            double turnoverDays = turnoverRate > 0 ? 365.0 / turnoverRate : 999;

            Material material = materialMapper.selectById(materialId);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("materialId", materialId);
            item.put("materialCode", material != null ? material.getCode() : "");
            item.put("materialName", material != null ? material.getName() : "未知物资");
            item.put("currentStock", currentQty);
            item.put("outQuantity", outQty);
            item.put("turnoverRate", Math.round(turnoverRate * 100.0) / 100.0);
            item.put("turnoverDays", Math.round(turnoverDays * 10.0) / 10.0);
            result.add(item);
        }

        // 按周转率降序排序
        result.sort((a, b) -> Double.compare(
                ((Number) b.get("turnoverRate")).doubleValue(),
                ((Number) a.get("turnoverRate")).doubleValue()));

        return result;
    }
}
