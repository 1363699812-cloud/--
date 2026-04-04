package com.wms.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.backend.entity.User;
import com.wms.backend.mapper.UserMapper;
import com.wms.backend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// @Service 注解标记这是一个 Service 实现类，会被 Spring 扫描到并管理
// 继承 MyBatis-Plus 提供的 ServiceImpl<M, T>
// 第一个泛型 M 是对应的 Mapper 类型 (UserMapper)
// 第二个泛型 T 是对应的实体类型 (User)
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    // 由于继承了 ServiceImpl<UserMapper, User>，它已经自动注入了 UserMapper，
    // 并实现了 IService<User> 中的所有方法。

    // 如果需要，你可以在这里注入其他组件或添加自定义的业务逻辑
    // 例如：
    // @Autowired
    // private SomeOtherComponent someOtherComponent;

    // 也可以重写父类或接口的方法，添加自己的逻辑
    // @Override
    // public boolean save(User entity) {
    //     // 在保存前做一些处理...
    //     boolean result = super.save(entity); // 调用父类的实现
    //     // 保存后做一些处理...
    //     return result;
    // }
}
