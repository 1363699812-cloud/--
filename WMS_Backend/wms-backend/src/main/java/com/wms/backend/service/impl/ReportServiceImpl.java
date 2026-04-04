package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wms.backend.entity.*;
import com.wms.backend.mapper.*;
import com.wms.backend.service.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        result.put("todayInboundCount", inboundOrderMapper.selectCount(
                new QueryWrapper<InboundOrder>().between("create_time", todayStart, todayEnd)));

        // 今日出库单数
        result.put("todayOutboundCount", outboundOrderMapper.selectCount(
                new QueryWrapper<OutboundOrder>().between("create_time", todayStart, todayEnd)));

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
    public Map<String, Object> getInboundStats(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        QueryWrapper<InboundOrder> qw = new QueryWrapper<>();
        if (startDate != null) {
            qw.ge("create_time", startDate + " 00:00:00");
        }
        if (endDate != null) {
            qw.le("create_time", endDate + " 23:59:59");
        }

        // 入库单总数
        result.put("totalCount", inboundOrderMapper.selectCount(qw));

        // 各状态数量
        for (int status = 0; status <= 2; status++) {
            QueryWrapper<InboundOrder> sq = new QueryWrapper<>();
            sq.eq("status", status);
            if (startDate != null) sq.ge("create_time", startDate + " 00:00:00");
            if (endDate != null) sq.le("create_time", endDate + " 23:59:59");
            result.put("status" + status + "Count", inboundOrderMapper.selectCount(sq));
        }

        // 总金额
        QueryWrapper<InboundOrder> amountQw = new QueryWrapper<>();
        amountQw.select("IFNULL(SUM(total_amount),0) as totalAmount");
        if (startDate != null) amountQw.ge("create_time", startDate + " 00:00:00");
        if (endDate != null) amountQw.le("create_time", endDate + " 23:59:59");
        List<Map<String, Object>> amountResult = inboundOrderMapper.selectMaps(amountQw);
        result.put("totalAmount", amountResult.isEmpty() ? 0 : amountResult.get(0).get("totalAmount"));

        return result;
    }

    @Override
    public Map<String, Object> getOutboundStats(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        QueryWrapper<OutboundOrder> qw = new QueryWrapper<>();
        if (startDate != null) {
            qw.ge("create_time", startDate + " 00:00:00");
        }
        if (endDate != null) {
            qw.le("create_time", endDate + " 23:59:59");
        }

        result.put("totalCount", outboundOrderMapper.selectCount(qw));

        for (int status = 0; status <= 2; status++) {
            QueryWrapper<OutboundOrder> sq = new QueryWrapper<>();
            sq.eq("status", status);
            if (startDate != null) sq.ge("create_time", startDate + " 00:00:00");
            if (endDate != null) sq.le("create_time", endDate + " 23:59:59");
            result.put("status" + status + "Count", outboundOrderMapper.selectCount(sq));
        }

        QueryWrapper<OutboundOrder> amountQw = new QueryWrapper<>();
        amountQw.select("IFNULL(SUM(total_amount),0) as totalAmount");
        if (startDate != null) amountQw.ge("create_time", startDate + " 00:00:00");
        if (endDate != null) amountQw.le("create_time", endDate + " 23:59:59");
        List<Map<String, Object>> amountResult = outboundOrderMapper.selectMaps(amountQw);
        result.put("totalAmount", amountResult.isEmpty() ? 0 : amountResult.get(0).get("totalAmount"));

        return result;
    }

    @Override
    public List<Map<String, Object>> getCategoryStats() {
        QueryWrapper<Material> qw = new QueryWrapper<>();
        qw.select("category_id AS categoryId", "COUNT(*) AS materialCount")
           .groupBy("category_id");
        List<Map<String, Object>> stats = materialMapper.selectMaps(qw);

        // 补充类别名称
        for (Map<String, Object> stat : stats) {
            Object categoryId = stat.get("categoryId");
            if (categoryId != null) {
                Category category = categoryMapper.selectById((Long) categoryId);
                stat.put("categoryName", category != null ? category.getName() : "未分类");
            } else {
                stat.put("categoryName", "未分类");
            }
        }
        return stats;
    }
}
