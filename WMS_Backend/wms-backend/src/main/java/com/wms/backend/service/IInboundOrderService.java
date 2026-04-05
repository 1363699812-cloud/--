// src/main/java/com/wms/backend/service/IInboundOrderService.java
package com.wms.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.backend.entity.InboundItem;
import com.wms.backend.entity.InboundOrder;
import java.util.List;

public interface IInboundOrderService extends IService<InboundOrder> {
    /**
     * 创建入库单
     */
    boolean createInboundOrder(InboundOrder order, List<InboundItem> items);

    /**
     * 审核入库单
     */
    boolean auditInboundOrder(Long orderId);

    /**
     * 完成入库单（更新库存）
     */
    boolean completeInboundOrder(Long orderId);

    boolean updateInboundOrder(Long orderId, InboundOrder order, List<InboundItem> items);
}