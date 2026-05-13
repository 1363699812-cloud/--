package com.wms.backend.service;

import java.util.List;
import java.util.Map;

public interface IReorderService {

    /**
     * 获取补货建议列表
     * 计算每种物资的ROP（补货点），筛选当前库存 <= ROP 的物资
     * @return 补货建议列表，含物资信息、当前库存、ROP、EOQ、缺口数量
     */
    List<Map<String, Object>> getReorderSuggestions();

    /**
     * 计算并更新所有物资的补货点(ROP)和经济订货批量(EOQ)
     * ROP = d̄ × L + SS
     * EOQ = sqrt(2DS/H)
     */
    void recalculateReorderParams();
}
