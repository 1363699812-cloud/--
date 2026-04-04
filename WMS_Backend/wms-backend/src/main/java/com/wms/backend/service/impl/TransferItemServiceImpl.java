package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.backend.entity.TransferItem;
import com.wms.backend.mapper.TransferItemMapper;
import com.wms.backend.service.ITransferItemService;
import org.springframework.stereotype.Service;

@Service
public class TransferItemServiceImpl extends ServiceImpl<TransferItemMapper, TransferItem>
        implements ITransferItemService {
}
