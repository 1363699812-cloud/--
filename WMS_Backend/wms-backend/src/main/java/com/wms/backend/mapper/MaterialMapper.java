package com.wms.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.backend.entity.Material; // 确保导入正确的实体类
// import org.apache.ibatis.annotations.Mapper; // 删除这个导入 (可选)

// @Mapper // 删除这一行！
public interface MaterialMapper extends BaseMapper<Material> {
    // 方法定义...
}