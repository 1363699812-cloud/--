package com.wms.backend.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 单据编号生成工具类
 * 格式: 前缀 + 日期(yyyyMMdd) + 3位序号，如 RK20260405001
 * 启动时从数据库查询当天最大序号，避免重启后序号重复
 */
@Component
public class OrderNoUtil {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);
    private static volatile String LAST_DATE = "";
    private static DataSource dataSource;

    @Autowired
    public void setDataSource(DataSource ds) {
        dataSource = ds;
    }

    /**
     * 从数据库查询当天所有单据的最大序号
     */
    private static int queryMaxSeqFromDb(String today) {
        String pattern = "%" + today + "___";
        String[] tables = {"inbound_order", "outbound_order", "transfer_order", "scrap_order", "stocktake_order"};
        int maxSeq = 0;
        if (dataSource == null) return 0;
        try (Connection conn = dataSource.getConnection()) {
            for (String table : tables) {
                String sql = "SELECT MAX(order_no) FROM " + table + " WHERE order_no LIKE ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, pattern);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next() && rs.getString(1) != null) {
                        String maxNo = rs.getString(1);
                        String seqStr = maxNo.substring(maxNo.length() - 3);
                        int seq = Integer.parseInt(seqStr);
                        if (seq > maxSeq) maxSeq = seq;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return maxSeq;
    }

    /**
     * 生成单据编号
     * @param prefix 前缀，如 RK(入库) CK(出库) DB(调拨) BS(报损) PD(盘点) PC(批次)
     * @return 单据编号
     */
    public static synchronized String generate(String prefix) {
        String today = LocalDate.now().format(DATE_FMT);
        if (!today.equals(LAST_DATE)) {
            LAST_DATE = today;
            int maxSeq = queryMaxSeqFromDb(today);
            SEQUENCE.set(maxSeq);
        }
        int seq = SEQUENCE.incrementAndGet();
        return String.format("%s%s%03d", prefix, today, seq);
    }
}
