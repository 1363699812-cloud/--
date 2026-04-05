-- WMS仓库管理系统 - 数据库初始化脚本
-- 数据库: wms_db (MySQL 8.0)

CREATE DATABASE IF NOT EXISTS wms_db DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE wms_db;

-- ========================================
-- 1. 用户表
-- ========================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role` VARCHAR(30) DEFAULT 'warehouse_keeper' COMMENT '角色: admin/warehouse_keeper/purchaser/seller',
    `status` INT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ========================================
-- 2. 仓库表
-- ========================================
CREATE TABLE IF NOT EXISTS `warehouse` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '仓库名称',
    `code` VARCHAR(50) DEFAULT NULL COMMENT '仓库编码',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '仓库地址',
    `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `capacity` INT DEFAULT NULL COMMENT '容量',
    `status` INT DEFAULT 1 COMMENT '状态 1-启用 0-停用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库表';

-- ========================================
-- 3. 分类表
-- ========================================
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `code` VARCHAR(50) DEFAULT NULL COMMENT '分类编码',
    `description` TEXT DEFAULT NULL COMMENT '分类描述',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父级分类ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` INT DEFAULT 1 COMMENT '状态 1-启用 0-停用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资分类表';

-- ========================================
-- 4. 物资表
-- ========================================
CREATE TABLE IF NOT EXISTS `material` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(50) NOT NULL COMMENT '物资编码',
    `name` VARCHAR(100) NOT NULL COMMENT '物资名称',
    `category_id` BIGINT DEFAULT NULL COMMENT '类别ID',
    `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位',
    `specification` VARCHAR(200) DEFAULT NULL COMMENT '规格型号',
    `min_stock` INT DEFAULT 0 COMMENT '最小库存',
    `max_stock` INT DEFAULT 99999 COMMENT '最大库存',
    `safety_stock` INT DEFAULT 0 COMMENT '安全库存',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `status` INT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资表';

-- ========================================
-- 5. 供应商表
-- ========================================
CREATE TABLE IF NOT EXISTS `supplier` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(50) NOT NULL COMMENT '供应商编码',
    `name` VARCHAR(100) NOT NULL COMMENT '供应商名称',
    `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `status` INT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- ========================================
-- 6. 客户表
-- ========================================
CREATE TABLE IF NOT EXISTS `customer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(50) NOT NULL COMMENT '客户编码',
    `name` VARCHAR(100) NOT NULL COMMENT '客户名称',
    `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `status` INT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- ========================================
-- 7. 库存表
-- ========================================
CREATE TABLE IF NOT EXISTS `inventory` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `material_id` BIGINT NOT NULL COMMENT '物资ID',
    `quantity` INT DEFAULT 0 COMMENT '库存数量',
    `lock_quantity` INT DEFAULT 0 COMMENT '锁定数量',
    `version` BIGINT DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_warehouse_material` (`warehouse_id`, `material_id`),
    KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- ========================================
-- 8. 入库单表
-- ========================================
CREATE TABLE IF NOT EXISTS `inbound_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_no` VARCHAR(50) NOT NULL COMMENT '入库单号',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `supplier_id` BIGINT DEFAULT NULL COMMENT '供应商ID',
    `total_amount` DECIMAL(12,2) DEFAULT 0 COMMENT '总金额',
    `status` INT DEFAULT 0 COMMENT '状态 0-待审核 1-已审核 2-已完成',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作员ID',
    `receive_date` DATE DEFAULT NULL COMMENT '接收日期',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_warehouse_id` (`warehouse_id`),
    KEY `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单表';

-- ========================================
-- 9. 入库明细表
-- ========================================
CREATE TABLE IF NOT EXISTS `inbound_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '入库单ID',
    `material_id` BIGINT NOT NULL COMMENT '物资ID',
    `quantity` INT NOT NULL COMMENT '数量',
    `unit_price` DECIMAL(10,2) DEFAULT 0 COMMENT '单价',
    `amount` DECIMAL(12,2) DEFAULT 0 COMMENT '金额',
    `batch_number` VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    `production_date` DATE DEFAULT NULL COMMENT '生产日期',
    `expiry_date` DATE DEFAULT NULL COMMENT '有效期',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库明细表';

-- ========================================
-- 10. 出库单表
-- ========================================
CREATE TABLE IF NOT EXISTS `outbound_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_no` VARCHAR(50) NOT NULL COMMENT '出库单号',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `customer_id` BIGINT DEFAULT NULL COMMENT '客户ID',
    `recipient` VARCHAR(50) DEFAULT NULL COMMENT '领用人',
    `department` VARCHAR(100) DEFAULT NULL COMMENT '部门',
    `total_amount` DECIMAL(12,2) DEFAULT 0 COMMENT '总金额',
    `status` INT DEFAULT 0 COMMENT '状态 0-待审核 1-已审核 2-已完成',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作员ID',
    `issue_date` DATE DEFAULT NULL COMMENT '发放日期',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_warehouse_id` (`warehouse_id`),
    KEY `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单表';

-- ========================================
-- 11. 出库明细表
-- ========================================
CREATE TABLE IF NOT EXISTS `outbound_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '出库单ID',
    `material_id` BIGINT NOT NULL COMMENT '物资ID',
    `quantity` INT NOT NULL COMMENT '数量',
    `unit_price` DECIMAL(10,2) DEFAULT 0 COMMENT '单价',
    `amount` DECIMAL(12,2) DEFAULT 0 COMMENT '金额',
    `batch_number` VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库明细表';

-- ========================================
-- 12. 库存流水表
-- ========================================
CREATE TABLE IF NOT EXISTS `inventory_transaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `material_id` BIGINT NOT NULL COMMENT '物资ID',
    `quantity` INT NOT NULL COMMENT '变化数量(正增负减)',
    `change_type` VARCHAR(20) NOT NULL COMMENT '变化类型 IN/OUT',
    `reference_type` VARCHAR(30) DEFAULT NULL COMMENT '关联类型 INBOUND/OUTBOUND/TRANSFER_IN/TRANSFER_OUT/DAMAGE/SCRAP/STOCKTAKE',
    `reference_id` VARCHAR(50) DEFAULT NULL COMMENT '关联ID',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `create_user` BIGINT DEFAULT NULL COMMENT '操作用户ID',
    PRIMARY KEY (`id`),
    KEY `idx_warehouse_material` (`warehouse_id`, `material_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

-- ========================================
-- 13. 调拨单表
-- ========================================
CREATE TABLE IF NOT EXISTS `transfer_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_no` VARCHAR(50) NOT NULL COMMENT '调拨单号',
    `from_warehouse_id` BIGINT NOT NULL COMMENT '源仓库ID',
    `to_warehouse_id` BIGINT NOT NULL COMMENT '目标仓库ID',
    `status` INT DEFAULT 0 COMMENT '状态 0-待审核 1-已审核 2-已完成',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作员ID',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调拨单表';

-- ========================================
-- 14. 调拨明细表
-- ========================================
CREATE TABLE IF NOT EXISTS `transfer_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '调拨单ID',
    `material_id` BIGINT NOT NULL COMMENT '物资ID',
    `quantity` INT NOT NULL COMMENT '调拨数量',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调拨明细表';

-- ========================================
-- 15. 报损报废单表
-- ========================================
CREATE TABLE IF NOT EXISTS `scrap_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_no` VARCHAR(50) NOT NULL COMMENT '报损单号',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `scrap_type` VARCHAR(20) NOT NULL COMMENT '类型 DAMAGE-报损 SCRAP-报废',
    `status` INT DEFAULT 0 COMMENT '状态 0-待审核 1-已审核 2-已完成',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作员ID',
    `reason` TEXT DEFAULT NULL COMMENT '报损原因',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报损报废单表';

-- ========================================
-- 16. 报损明细表
-- ========================================
CREATE TABLE IF NOT EXISTS `scrap_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '报损单ID',
    `material_id` BIGINT NOT NULL COMMENT '物资ID',
    `quantity` INT NOT NULL COMMENT '报损数量',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报损明细表';

-- ========================================
-- 17. 盘点单表
-- ========================================
CREATE TABLE IF NOT EXISTS `stocktake_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_no` VARCHAR(50) NOT NULL COMMENT '盘点单号',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `status` INT DEFAULT 0 COMMENT '状态 0-进行中 1-已完成',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作员ID',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点单表';

-- ========================================
-- 18. 盘点明细表
-- ========================================
CREATE TABLE IF NOT EXISTS `stocktake_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '盘点单ID',
    `material_id` BIGINT NOT NULL COMMENT '物资ID',
    `system_quantity` INT DEFAULT 0 COMMENT '系统数量',
    `actual_quantity` INT DEFAULT NULL COMMENT '实盘数量',
    `diff_quantity` INT DEFAULT NULL COMMENT '差异数量',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点明细表';

-- ========================================
-- 19. 操作日志表
-- ========================================
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
    `module` VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
    `operation` VARCHAR(200) DEFAULT NULL COMMENT '操作描述',
    `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    `params` TEXT DEFAULT NULL COMMENT '请求参数',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT '请求IP',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ========================================
-- 初始数据
-- ========================================

-- 管理员账号 (密码: admin123)
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`) VALUES
('admin', 'admin123', '系统管理员', 'admin', 1);

-- 测试仓库
INSERT INTO `warehouse` (`name`, `code`, `address`, `contact_person`, `phone`, `status`) VALUES
('主仓库', 'WH001', '厂区A栋1楼', '张三', '13800138001', 1),
('备用仓库', 'WH002', '厂区B栋2楼', '李四', '13800138002', 1);

-- 测试分类
INSERT INTO `category` (`name`, `code`, `description`, `status`) VALUES
('电子元器件', 'CAT001', '各类电子元件', 1),
('机械零件', 'CAT002', '机械加工零部件', 1),
('办公用品', 'CAT003', '日常办公耗材', 1);

-- 测试物资
INSERT INTO `material` (`code`, `name`, `category_id`, `unit`, `specification`, `min_stock`, `max_stock`, `safety_stock`, `status`) VALUES
('MAT001', '电阻10K', 1, '个', '0805贴片', 100, 10000, 500, 1),
('MAT002', '电容100nF', 1, '个', '0603贴片', 200, 20000, 1000, 1),
('MAT003', 'M6螺栓', 2, '个', 'M6x20不锈钢', 50, 5000, 200, 1),
('MAT004', 'A4打印纸', 3, '包', '70g 500张/包', 10, 500, 50, 1);

-- 测试供应商
INSERT INTO `supplier` (`code`, `name`, `contact_person`, `phone`, `email`, `address`, `status`) VALUES
('SUP001', '华星电子有限公司', '王经理', '13900139001', 'wang@huaxing.com', '深圳市南山区', 1),
('SUP002', '恒达机械制造厂', '赵经理', '13900139002', 'zhao@hengda.com', '东莞市长安镇', 1);

-- 测试客户
INSERT INTO `customer` (`code`, `name`, `contact_person`, `phone`, `email`, `address`, `status`) VALUES
('CUS001', '光明科技有限公司', '刘总', '13700137001', 'liu@guangming.com', '广州市天河区', 1),
('CUS002', '盛世实业集团', '陈总', '13700137002', 'chen@shengshi.com', '佛山市顺德区', 1);

-- 测试库存
INSERT INTO `inventory` (`warehouse_id`, `material_id`, `quantity`, `lock_quantity`, `version`) VALUES
(1, 1, 3000, 0, 0),
(1, 2, 8000, 0, 0),
(1, 3, 1000, 0, 0),
(1, 4, 100, 0, 0),
(2, 1, 500, 0, 0),
(2, 3, 300, 0, 0);
