package com.wms.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.backend.entity.User;

// 继承 MyBatis-Plus 提供的 IService<T> 接口
// 这样就自动拥有了 IService 中定义的通用业务方法
public interface IUserService extends IService<User> {
    // 可以在这里定义你自己特有的业务方法
    // 例如：User findUserWithDetails(Long userId);
    // 基本的 CRUD 已经继承自 IService
}