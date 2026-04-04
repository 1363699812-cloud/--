// src/main/java/com/wms/backend/service/IMaterialService.java
package com.wms.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.backend.entity.Material;

public interface IMaterialService extends IService<Material> {
    // 这里可以定义自定义的业务方法
    // 目前继承 IService 已经拥有了基本的 CRUD 能力
}