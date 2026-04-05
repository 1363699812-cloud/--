import request from '@/utils/request'

// ==================== 用户 ====================
export const login = (data) => request.post('/user/login', data)
export const register = (data) => request.post('/user/register', data)
export const getUserList = (params) => request.get('/user/list', { params })
export const saveUser = (data) => request.post('/user', data)
export const updateUser = (id, data) => request.put(`/user/${id}`, data)
export const deleteUser = (id) => request.delete(`/user/${id}`)

// ==================== 仓库 ====================
export const getWarehouseList = (params) => request.get('/warehouse/list', { params })
export const getAllWarehouses = () => request.get('/warehouse/all')
export const saveWarehouse = (data) => request.post('/warehouse', data)
export const updateWarehouse = (id, data) => request.put(`/warehouse/${id}`, data)
export const deleteWarehouse = (id) => request.delete(`/warehouse/${id}`)
export const toggleWarehouseStatus = (id) => request.post(`/warehouse/${id}/toggle-status`)

// ==================== 分类 ====================
export const getCategoryList = (params) => request.get('/category/list', { params })
export const getAllCategories = () => request.get('/category/all')
export const saveCategory = (data) => request.post('/category', data)
export const updateCategory = (id, data) => request.put(`/category/${id}`, data)
export const deleteCategory = (id) => request.delete(`/category/${id}`)

// ==================== 物资 ====================
export const getMaterialList = (params) => request.get('/material/list', { params })
export const getAllMaterials = () => request.get('/material/all')
export const saveMaterial = (data) => request.post('/material', data)
export const updateMaterial = (id, data) => request.put(`/material/${id}`, data)
export const deleteMaterial = (id) => request.delete(`/material/${id}`)

// ==================== 供应商 ====================
export const getSupplierList = (params) => request.get('/supplier/list', { params })
export const getAllSuppliers = () => request.get('/supplier/all')
export const saveSupplier = (data) => request.post('/supplier', data)
export const updateSupplier = (id, data) => request.put(`/supplier/${id}`, data)
export const deleteSupplier = (id) => request.delete(`/supplier/${id}`)

// ==================== 客户 ====================
export const getCustomerList = (params) => request.get('/customer/list', { params })
export const getAllCustomers = () => request.get('/customer/all')
export const saveCustomer = (data) => request.post('/customer', data)
export const updateCustomer = (id, data) => request.put(`/customer/${id}`, data)
export const deleteCustomer = (id) => request.delete(`/customer/${id}`)

// ==================== 入库单 ====================
export const getInboundOrderList = (params) => request.get('/inbound-order/list', { params })
export const getInboundOrder = (id) => request.get(`/inbound-order/${id}`)
export const getInboundItems = (orderId) => request.get(`/inbound-order/${orderId}/items`)
export const createInboundOrder = (data) => request.post('/inbound-order', data)
export const auditInboundOrder = (orderId) => request.post(`/inbound-order/${orderId}/audit`)
export const rejectInboundOrder = (orderId) => request.post(`/inbound-order/${orderId}/reject`)
export const completeInboundOrder = (orderId) => request.post(`/inbound-order/${orderId}/complete`)
export const deleteInboundOrder = (id) => request.delete(`/inbound-order/${id}`)
export const updateInboundOrder = (id, data) => request.put(`/inbound-order/${id}`, data)

// ==================== 出库单 ====================
export const getOutboundOrderList = (params) => request.get('/outbound-order/list', { params })
export const getOutboundOrder = (id) => request.get(`/outbound-order/${id}`)
export const getOutboundItems = (orderId) => request.get(`/outbound-order/${orderId}/items`)
export const createOutboundOrder = (data) => request.post('/outbound-order', data)
export const auditOutboundOrder = (orderId) => request.post(`/outbound-order/${orderId}/audit`)
export const rejectOutboundOrder = (orderId) => request.post(`/outbound-order/${orderId}/reject`)
export const completeOutboundOrder = (orderId) => request.post(`/outbound-order/${orderId}/complete`)
export const deleteOutboundOrder = (id) => request.delete(`/outbound-order/${id}`)
export const updateOutboundOrder = (id, data) => request.put(`/outbound-order/${id}`, data)

// ==================== 库存 ====================
export const getInventoryList = (params) => request.get('/inventory/list', { params })
export const getStockAlerts = () => request.get('/inventory/alerts')

// ==================== 库存流水 ====================
export const getTransactionList = (params) => request.get('/inventory-transaction/list', { params })

// ==================== 调拨 ====================
export const getTransferOrderList = (params) => request.get('/transfer-order/list', { params })
export const getTransferOrder = (id) => request.get(`/transfer-order/${id}`)
export const getTransferItems = (orderId) => request.get(`/transfer-order/${orderId}/items`)
export const createTransferOrder = (data) => request.post('/transfer-order', data)
export const auditTransferOrder = (orderId) => request.post(`/transfer-order/${orderId}/audit`)
export const rejectTransferOrder = (orderId) => request.post(`/transfer-order/${orderId}/reject`)
export const completeTransferOrder = (orderId) => request.post(`/transfer-order/${orderId}/complete`)
export const deleteTransferOrder = (id) => request.delete(`/transfer-order/${id}`)

// ==================== 报损报废 ====================
export const getScrapOrderList = (params) => request.get('/scrap-order/list', { params })
export const getScrapOrder = (id) => request.get(`/scrap-order/${id}`)
export const getScrapItems = (orderId) => request.get(`/scrap-order/${orderId}/items`)
export const createScrapOrder = (data) => request.post('/scrap-order', data)
export const auditScrapOrder = (orderId) => request.post(`/scrap-order/${orderId}/audit`)
export const rejectScrapOrder = (orderId) => request.post(`/scrap-order/${orderId}/reject`)
export const completeScrapOrder = (orderId) => request.post(`/scrap-order/${orderId}/complete`)
export const deleteScrapOrder = (id) => request.delete(`/scrap-order/${id}`)

// ==================== 盘点 ====================
export const getStocktakeOrderList = (params) => request.get('/stocktake/list', { params })
export const getStocktakeOrder = (id) => request.get(`/stocktake/${id}`)
export const getStocktakeItems = (orderId) => request.get(`/stocktake/${orderId}/items`)
export const createStocktakeOrder = (data) => request.post('/stocktake', data)
export const completeStocktakeOrder = (orderId) => request.post(`/stocktake/${orderId}/complete`)
export const updateStocktakeItem = (itemId, data) => request.put(`/stocktake/item/${itemId}`, data)
export const deleteStocktakeOrder = (id) => request.delete(`/stocktake/${id}`)

// ==================== 报表 ====================
export const getDashboard = () => request.get('/report/dashboard')
export const getInboundStats = (params) => request.get('/report/inbound-stats', { params })
export const getOutboundStats = (params) => request.get('/report/outbound-stats', { params })
export const getCategoryStats = () => request.get('/report/category-stats')
export const getInventorySummary = (params) => request.get('/report/inventory-summary', { params })
export const getAbcAnalysis = () => request.get('/report/abc-analysis')
export const getTurnoverRate = (params) => request.get('/report/turnover-rate', { params })

// ==================== 操作日志 ====================
export const getOperationLogList = (params) => request.get('/operation-log/list', { params })
