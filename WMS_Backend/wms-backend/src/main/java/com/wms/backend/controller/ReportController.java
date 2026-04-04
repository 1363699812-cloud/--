package com.wms.backend.controller;

import com.wms.backend.common.Result;
import com.wms.backend.service.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private IReportService reportService;

    @GetMapping("/dashboard")
    public Result dashboard() {
        return Result.success(reportService.getDashboard());
    }

    @GetMapping("/inventory-summary")
    public Result inventorySummary(@RequestParam(required = false) Long warehouseId) {
        return Result.success(reportService.getInventorySummary(warehouseId));
    }

    @GetMapping("/inbound-stats")
    public Result inboundStats(@RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate) {
        return Result.success(reportService.getInboundStats(startDate, endDate));
    }

    @GetMapping("/outbound-stats")
    public Result outboundStats(@RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate) {
        return Result.success(reportService.getOutboundStats(startDate, endDate));
    }

    @GetMapping("/category-stats")
    public Result categoryStats() {
        return Result.success(reportService.getCategoryStats());
    }
}
