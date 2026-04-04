package com.wms.backend.service;

import java.util.List;
import java.util.Map;

public interface IReportService {

    Map<String, Object> getDashboard();

    List<Map<String, Object>> getInventorySummary(Long warehouseId);

    Map<String, Object> getInboundStats(String startDate, String endDate);

    Map<String, Object> getOutboundStats(String startDate, String endDate);

    List<Map<String, Object>> getCategoryStats();
}
