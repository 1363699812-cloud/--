package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.backend.entity.Material;
import com.wms.backend.mapper.MaterialMapper;
import com.wms.backend.service.IMaterialService;
import org.springframework.stereotype.Service;

@Service
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material> implements IMaterialService {
    // 具体的业务逻辑实现可以在这里写
}