package com.wms.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.backend.entity.TransferItem;
import com.wms.backend.entity.TransferOrder;
import java.util.List;

public interface ITransferOrderService extends IService<TransferOrder> {
    boolean createTransferOrder(TransferOrder order, List<TransferItem> items);
    boolean auditTransferOrder(Long orderId);
    boolean completeTransferOrder(Long orderId);
}
