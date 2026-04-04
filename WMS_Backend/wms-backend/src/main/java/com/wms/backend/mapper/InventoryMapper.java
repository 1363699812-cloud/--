// src/main/java/com/wms/backend/mapper/InventoryMapper.java
package com.wms.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.backend.entity.Inventory;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface InventoryMapper extends BaseMapper<Inventory> {

    @Select("SELECT i.id, i.warehouse_id AS warehouseId, i.material_id AS materialId, " +
            "i.quantity, m.name AS materialName, m.code AS materialCode, " +
            "m.safety_stock AS safetyStock, m.max_stock AS maxStock, m.min_stock AS minStock, " +
            "w.name AS warehouseName, " +
            "CASE WHEN i.quantity < m.safety_stock THEN 'LOW' " +
            "WHEN i.quantity > m.max_stock THEN 'HIGH' END AS alertType " +
            "FROM inventory i " +
            "LEFT JOIN material m ON i.material_id = m.id " +
            "LEFT JOIN warehouse w ON i.warehouse_id = w.id " +
            "WHERE i.quantity < m.safety_stock OR i.quantity > m.max_stock")
    List<Map<String, Object>> selectStockAlerts();
}
