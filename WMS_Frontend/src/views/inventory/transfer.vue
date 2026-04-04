<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="单据编号">
          <el-input v-model="query.orderNo" placeholder="请输入单据编号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable>
            <el-option label="待审核" :value="0" />
            <el-option label="已审核" :value="1" />
            <el-option label="已完成" :value="2" />
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
        <el-button type="primary" @click="openDialog()">新建调拨单</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="orderNo" label="单据编号" width="180" />
        <el-table-column prop="fromWarehouseId" label="调出仓库" width="130">
          <template #default="{ row }">{{ getWarehouseName(row.fromWarehouseId) }}</template>
        </el-table-column>
        <el-table-column prop="toWarehouseId" label="调入仓库" width="130">
          <template #default="{ row }">{{ getWarehouseName(row.toWarehouseId) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button link type="warning" v-if="row.status === 0" @click="handleAudit(row.id)">审核</el-button>
            <el-button link type="success" v-if="row.status === 1" @click="handleComplete(row.id)">执行调拨</el-button>
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

    <!-- 新建调拨单-->
    <el-dialog v-model="dialogVisible" title="新建调拨单" width="800px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="调出仓库" prop="fromWarehouseId">
              <el-select v-model="form.fromWarehouseId" placeholder="请选择" style="width:100%;">
                <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="调入仓库" prop="toWarehouseId">
              <el-select v-model="form.toWarehouseId" placeholder="请选择" style="width:100%;">
                <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
        <div class="items-header">
          <span style="font-weight: bold;">调拨明细</span>
          <el-button type="primary" size="small" @click="form.items.push({ materialId: null, quantity: 1, remark: '' })">添加物资</el-button>
        </div>
        <el-table :data="form.items" border size="small" style="margin-top: 8px;">
          <el-table-column label="物资" min-width="200">
            <template #default="{ row }">
              <el-select v-model="row.materialId" placeholder="请选择" filterable style="width:100%;">
                <el-option v-for="m in materials" :key="m.id" :label="`${m.code} - ${m.name}`" :value="m.id" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="120">
            <template #default="{ row }"><el-input-number v-model="row.quantity" :min="1" size="small" style="width:100%;" /></template>
          </el-table-column>
          <el-table-column label="备注" width="160">
            <template #default="{ row }"><el-input v-model="row.remark" size="small" /></template>
          </el-table-column>
          <el-table-column label="操作" width="60" align="center">
            <template #default="{ $index }"><el-button link type="danger" @click="form.items.splice($index, 1)">删除</el-button></template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="调拨单详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="单据编号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="statusMap[detail.status]?.type">{{ statusMap[detail.status]?.label }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="调出仓库">{{ getWarehouseName(detail.fromWarehouseId) }}</el-descriptions-item>
        <el-descriptions-item label="调入仓库">{{ getWarehouseName(detail.toWarehouseId) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detailItems" border size="small" style="margin-top: 16px;">
        <el-table-column prop="materialId" label="物资"><template #default="{ row }">{{ getMaterialName(row.materialId) }}</template></el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="remark" label="备注" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getTransferOrderList, createTransferOrder, auditTransferOrder, completeTransferOrder,
  deleteTransferOrder, getTransferItems, getAllWarehouses, getAllMaterials
} from '@/api'

const statusMap = { 0: { label: '待审核', type: 'warning' }, 1: { label: '已审核', type: '' }, 2: { label: '已完成', type: 'success' } }

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const formRef = ref()
const tableData = ref([])
const total = ref(0)
const warehouses = ref([])
const materials = ref([])
const detail = reactive({})
const detailItems = ref([])

const query = reactive({ orderNo: '', status: null, current: 1, size: 10 })
const form = reactive({ fromWarehouseId: null, toWarehouseId: null, remark: '', items: [] })

const rules = {
  fromWarehouseId: [{ required: true, message: '请选择调出仓库', trigger: 'change' }],
  toWarehouseId: [{ required: true, message: '请选择调入仓库', trigger: 'change' }],
}

const getWarehouseName = (id) => warehouses.value.find((w) => w.id === id)?.name || id
const getMaterialName = (id) => materials.value.find((m) => m.id === id)?.name || id

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTransferOrderList(query)
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } finally { loading.value = false }
}

const handleSearch = () => { query.current = 1; loadData() }
const resetQuery = () => { Object.assign(query, { orderNo: '', status: null, current: 1 }); loadData() }

const openDialog = () => {
  Object.assign(form, { fromWarehouseId: null, toWarehouseId: null, remark: '', items: [{ materialId: null, quantity: 1, remark: '' }] })
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    if (form.fromWarehouseId === form.toWarehouseId) { ElMessage.warning('调出和调入仓库不能相同'); return }
    if (!form.items.length || form.items.some((i) => !i.materialId)) { ElMessage.warning('请至少添加一条物资明细'); return }
    submitting.value = true
    try {
      const res = await createTransferOrder(form)
      if (res.code === 200) { ElMessage.success('创建成功'); dialogVisible.value = false; loadData() }
    } finally { submitting.value = false }
  })
}

const viewDetail = async (row) => {
  Object.assign(detail, row)
  const res = await getTransferItems(row.id)
  if (res.code === 200) detailItems.value = res.data
  detailVisible.value = true
}

const handleAudit = (id) => {
  ElMessageBox.confirm('确认审核通过该调拨单吗', '提示', { type: 'warning' }).then(async () => {
    const res = await auditTransferOrder(id)
    if (res.code === 200) { ElMessage.success('审核成功'); loadData() }
  }).catch(() => {})
}

const handleComplete = (id) => {
  ElMessageBox.confirm('确认执行调拨？库存将在仓库间转移', '提示', { type: 'warning' }).then(async () => {
    const res = await completeTransferOrder(id)
    if (res.code === 200) { ElMessage.success('调拨完成'); loadData() }
  }).catch(() => {})
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该调拨单吗', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteTransferOrder(id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadData() }
  }).catch(() => {})
}

onMounted(async () => {
  const [w, m] = await Promise.all([getAllWarehouses(), getAllMaterials()])
  if (w.data.code === 200) warehouses.value = w.data.data
  if (m.data.code === 200) materials.value = m.data.data
  loadData()
})
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
.items-header { display: flex; justify-content: space-between; align-items: center; margin-top: 16px; }
</style>
