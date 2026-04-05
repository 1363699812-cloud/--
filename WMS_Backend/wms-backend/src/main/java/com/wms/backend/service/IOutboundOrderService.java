// src/main/java/com/wms/backend/service/IOutboundOrderService.java
package com.wms.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.backend.entity.OutboundItem;
import com.wms.backend.entity.OutboundOrder;
import java.util.List;

public interface IOutboundOrderService extends IService<OutboundOrder> {
    /**
     * 创建出库单
     */
    boolean createOutboundOrder(OutboundOrder order, List<OutboundItem> items);

    /**
     * 审核出库单
     */
    boolean auditOutboundOrder(Long orderId);

    /**
     * 完成出库单（扣减库存）
     */
    boolean completeOutboundOrder(Long orderId);

    /**
     * 检查出库单是否有足够库存
     */
    boolean checkStockForOutbound(Long orderId);

    boolean updateOutboundOrder(Long orderId, OutboundOrder order, List<OutboundItem> items);

    boolean rejectOutboundOrder(Long orderId);
}