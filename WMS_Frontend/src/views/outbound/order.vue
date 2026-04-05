<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="单据编号">
          <el-input v-model="query.orderNo" placeholder="请输入单据编号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px;">
            <el-option label="待审核" :value="0" />
            <el-option label="已审核" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已驳回" :value="-1" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户">
          <el-select v-model="query.customerId" placeholder="全部" clearable filterable style="width: 200px;">
            <el-option v-for="c in customers" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 12px;">
      <div class="toolbar">
        <el-button type="primary" @click="openDialog()">新建出库单</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="orderNo" label="单据编号" width="180" />
        <el-table-column prop="warehouseId" label="仓库" width="120">
          <template #default="{ row }">{{ getWarehouseName(row.warehouseId) }}</template>
        </el-table-column>
        <el-table-column prop="customerId" label="客户" width="140">
          <template #default="{ row }">{{ getCustomerName(row.customerId) }}</template>
        </el-table-column>
        <el-table-column prop="recipient" label="领用人" width="100" />
        <el-table-column prop="totalAmount" label="总金额" width="110" align="right">
          <template #default="{ row }">{{ row.totalAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="issueDate" label="发货日期" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button link type="primary" v-if="row.status === 0" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="warning" v-if="row.status === 0" @click="handleAudit(row.id)">审核</el-button>
            <el-button link type="success" v-if="row.status === 1" @click="handleComplete(row.id)">出库</el-button>
            <el-button link type="danger" v-if="row.status === 0" @click="handleReject(row.id)">驳回</el-button>
            <el-button link type="danger" v-if="row.status === 0" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end;"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 新建出库单-->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑出库单' : '新建出库单'" width="850px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" placeholder="请选择" style="width:100%;">
                <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择" filterable style="width:100%;">
                <el-option v-for="c in customers" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="发货日期">
              <el-date-picker v-model="form.issueDate" type="date" value-format="YYYY-MM-DD" style="width:100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="领用人"><el-input v-model="form.recipient" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="部门"><el-input v-model="form.department" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>

        <div class="items-header">
          <span style="font-weight: bold;">出库明细</span>
          <el-button type="primary" size="small" @click="addItem">添加物资</el-button>
        </div>
        <el-table :data="form.items" border size="small" style="margin-top: 8px;">
          <el-table-column label="物资" min-width="180">
            <template #default="{ row }">
              <el-select v-model="row.materialId" placeholder="请选择物资" filterable style="width:100%;">
                <el-option v-for="m in materials" :key="m.id" :label="`${m.code} - ${m.name}`" :value="m.id" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="110">
            <template #default="{ row }"><el-input-number v-model="row.quantity" :min="1" size="small" style="width:100%;" /></template>
          </el-table-column>
          <el-table-column label="单价" width="110">
            <template #default="{ row }"><el-input-number v-model="row.unitPrice" :min="0" :precision="2" size="small" style="width:100%;" /></template>
          </el-table-column>
          <el-table-column label="金额" width="100">
            <template #default="{ row }">{{ ((row.quantity || 0) * (row.unitPrice || 0)).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="批次号" width="160">
            <template #default="{ row }"><el-input v-model="row.batchNumber" size="small" placeholder="留空自动生成" /></template>
          </el-table-column>
          <el-table-column label="操作" width="60" align="center">
            <template #default="{ $index }">
              <el-button link type="danger" @click="form.items.splice($index, 1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="出库单详情" width="750px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="单据编号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusMap[detail.status]?.type">{{ statusMap[detail.status]?.label }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="仓库">{{ getWarehouseName(detail.warehouseId) }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ getCustomerName(detail.customerId) }}</el-descriptions-item>
        <el-descriptions-item label="领用人">{{ detail.recipient }}</el-descriptions-item>
        <el-descriptions-item label="总金额">{{ detail.totalAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="发货日期">{{ detail.issueDate }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.remark }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detailItems" border size="small" style="margin-top: 16px;">
        <el-table-column prop="materialId" label="物资">
          <template #default="{ row }">{{ getMaterialName(row.materialId) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="90" />
        <el-table-column prop="unitPrice" label="单价" width="100">
          <template #default="{ row }">{{ row.unitPrice?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">{{ row.amount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="batchNumber" label="批次号" width="130" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOutboundOrderList, createOutboundOrder, updateOutboundOrder, auditOutboundOrder, rejectOutboundOrder, completeOutboundOrder,
  deleteOutboundOrder, getOutboundItems, getAllWarehouses, getAllCustomers, getAllMaterials
} from '@/api'

const statusMap = { '-1': { label: '已驳回', type: 'danger' }, 0: { label: '待审核', type: 'warning' }, 1: { label: '已审核', type: '' }, 2: { label: '已完成', type: 'success' } }

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const formRef = ref()
const tableData = ref([])
const total = ref(0)
const warehouses = ref([])
const customers = ref([])
const materials = ref([])
const detail = reactive({})
const detailItems = ref([])
const editingId = ref(null)

const query = reactive({ orderNo: '', status: null, customerId: null, current: 1, size: 10 })
const form = reactive({ warehouseId: null, customerId: null, recipient: '', department: '', issueDate: '', remark: '', items: [] })

const rules = {
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
}

const getWarehouseName = (id) => warehouses.value.find((w) => w.id === id)?.name || id
const getCustomerName = (id) => customers.value.find((c) => c.id === id)?.name || id
const getMaterialName = (id) => materials.value.find((m) => m.id === id)?.name || id

const addItem = () => form.items.push({ materialId: null, quantity: 1, unitPrice: 0, batchNumber: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getOutboundOrderList(query)
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } finally { loading.value = false }
}

const handleSearch = () => { query.current = 1; loadData() }
const resetQuery = () => { Object.assign(query, { orderNo: '', status: null, customerId: null, current: 1 }); loadData() }

const openDialog = () => {
  editingId.value = null
  Object.assign(form, { warehouseId: null, customerId: null, recipient: '', department: '', issueDate: '', remark: '', items: [] })
  addItem()
  dialogVisible.value = true
}

const openEditDialog = async (row) => {
  editingId.value = row.id
  Object.assign(form, { warehouseId: row.warehouseId, customerId: row.customerId, recipient: row.recipient, department: row.department, issueDate: row.issueDate, remark: row.remark, items: [] })
  const res = await getOutboundItems(row.id)
  if (res.code === 200) {
    form.items = res.data.map(item => ({
      materialId: item.materialId, quantity: item.quantity, unitPrice: item.unitPrice, batchNumber: item.batchNumber
    }))
  }
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    if (!form.items.length || form.items.some((i) => !i.materialId)) {
      ElMessage.warning('请至少添加一条物资明细'); return
    }
    submitting.value = true
    try {
      const submitData = {
        order: { warehouseId: form.warehouseId, customerId: form.customerId, recipient: form.recipient, department: form.department, issueDate: form.issueDate, remark: form.remark },
        items: form.items.map(i => ({ materialId: i.materialId, quantity: i.quantity, unitPrice: i.unitPrice, amount: (i.quantity || 0) * (i.unitPrice || 0), batchNumber: i.batchNumber }))
      }
      const res = editingId.value
        ? await updateOutboundOrder(editingId.value, submitData)
        : await createOutboundOrder(submitData)
      if (res.code === 200) { ElMessage.success(editingId.value ? '更新成功' : '创建成功'); dialogVisible.value = false; loadData() }
    } finally { submitting.value = false }
  })
}

const viewDetail = async (row) => {
  Object.assign(detail, row)
  const res = await getOutboundItems(row.id)
  if (res.code === 200) detailItems.value = res.data
  detailVisible.value = true
}

const handleAudit = (id) => {
  ElMessageBox.confirm('确认审核通过该出库单吗', '提示', { type: 'warning' }).then(async () => {
    const res = await auditOutboundOrder(id)
    if (res.code === 200) { ElMessage.success('审核成功'); loadData() }
  }).catch(() => {})
}

const handleReject = (id) => {
  ElMessageBox.confirm('确认驳回该出库单吗', '提示', { type: 'warning' }).then(async () => {
    const res = await rejectOutboundOrder(id)
    if (res.code === 200) { ElMessage.success('已驳回'); loadData() }
  }).catch(() => {})
}

const handleComplete = (id) => {
  ElMessageBox.confirm('确认执行出库操作？物资将从仓库中扣减', '提示', { type: 'warning' }).then(async () => {
    const res = await completeOutboundOrder(id)
    if (res.code === 200) { ElMessage.success('出库成功'); loadData() }
  }).catch(() => {})
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该出库单吗', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteOutboundOrder(id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadData() }
  }).catch(() => {})
}

onMounted(async () => {
  const [w, c, m] = await Promise.all([getAllWarehouses(), getAllCustomers(), getAllMaterials()])
  if (w.code === 200) warehouses.value = w.data
  if (c.code === 200) customers.value = c.data
  if (m.code === 200) materials.value = m.data
  loadData()
})
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
.items-header { display: flex; justify-content: space-between; align-items: center; margin-top: 16px; }
</style>
