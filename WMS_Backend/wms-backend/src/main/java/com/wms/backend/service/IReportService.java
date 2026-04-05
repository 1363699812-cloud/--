package com.wms.backend.service;

import java.util.List;
import java.util.Map;

public interface IReportService {

    Map<String, Object> getDashboard();

    List<Map<String, Object>> getInventorySummary(Long warehouseId);

    List<Map<String, Object>> getInboundStats(String startDate, String endDate);

    List<Map<String, Object>> getOutboundStats(String startDate, String endDate);

    List<Map<String, Object>> getCategoryStats();

    List<Map<String, Object>> getAbcAnalysis();

    List<Map<String, Object>> getTurnoverRate(String startDate, String endDate);
}
