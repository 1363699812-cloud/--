// src/main/java/com/wms/backend/service/ICategoryService.java
package com.wms.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.backend.entity.Category;

public interface ICategoryService extends IService<Category> {
    // 继承IService已提供基础CRUD功能
}