package com.wms.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.backend.entity.StocktakeItem;
import com.wms.backend.entity.StocktakeOrder;
import java.util.List;

public interface IStocktakeOrderService extends IService<StocktakeOrder> {
    boolean createStocktakeOrder(StocktakeOrder order, List<StocktakeItem> items);
    boolean completeStocktakeOrder(Long orderId);
}
