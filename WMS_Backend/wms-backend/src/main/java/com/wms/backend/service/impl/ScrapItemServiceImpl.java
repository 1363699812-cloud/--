package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.backend.entity.ScrapItem;
import com.wms.backend.mapper.ScrapItemMapper;
import com.wms.backend.service.IScrapItemService;
import org.springframework.stereotype.Service;

@Service
public class ScrapItemServiceImpl extends ServiceImpl<ScrapItemMapper, ScrapItem>
        implements IScrapItemService {
}
