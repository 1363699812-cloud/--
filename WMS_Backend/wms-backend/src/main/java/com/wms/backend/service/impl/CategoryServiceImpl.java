// src/main/java/com/wms/backend/service/impl/CategoryServiceImpl.java
package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.backend.entity.Category;
import com.wms.backend.mapper.CategoryMapper;
import com.wms.backend.service.ICategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {
    // 继承ServiceImpl已提供基础CRUD功能
}