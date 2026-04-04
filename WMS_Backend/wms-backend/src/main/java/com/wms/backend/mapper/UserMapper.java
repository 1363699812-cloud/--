package com.wms.backend.mapper; // 确保包名正确

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.backend.entity.User; // 导入你的 User 实体类
// import org.apache.ibatis.annotations.Mapper; // 删除这个导入 (可选，留着也无妨，但注解要删)

// @Mapper // 删除这一行！
public interface UserMapper extends BaseMapper<User> {
    // 你可以在这里定义自己的特殊查询方法，例如：
    // List<User> selectByCustomCondition(@Param("condition") String condition);
    // 但基本的 CRUD 不需要写任何方法，都已继承自 BaseMapper
}