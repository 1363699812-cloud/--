package com.wms.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.backend.entity.ScrapItem;
import com.wms.backend.entity.ScrapOrder;
import java.util.List;

public interface IScrapOrderService extends IService<ScrapOrder> {
    boolean createScrapOrder(ScrapOrder order, List<ScrapItem> items);
    boolean auditScrapOrder(Long orderId);
    boolean completeScrapOrder(Long orderId);
}
