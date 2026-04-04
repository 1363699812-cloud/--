package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.backend.entity.StocktakeItem;
import com.wms.backend.mapper.StocktakeItemMapper;
import com.wms.backend.service.IStocktakeItemService;
import org.springframework.stereotype.Service;

@Service
public class StocktakeItemServiceImpl extends ServiceImpl<StocktakeItemMapper, StocktakeItem>
        implements IStocktakeItemService {
}
