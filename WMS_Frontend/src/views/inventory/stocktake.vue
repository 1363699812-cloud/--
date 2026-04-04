<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="单据编号">
          <el-input v-model="query.orderNo" placeholder="请输入单据编号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable>
            <el-option label="盘点中" :value="0" />
            <el-option label="已完成" :value="1" />
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
        <el-button type="primary" @click="openDialog()">新建盘点单</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="orderNo" label="单据编号" width="180" />
        <el-table-column prop="warehouseId" label="仓库" width="150">
          <template #default="{ row }">{{ getWarehouseName(row.warehouseId) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'">{{ row.status === 0 ? '盘点中' : '已完成' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情/录入</el-button>
            <el-button link type="success" v-if="row.status === 0" @click="handleComplete(row.id)">完成盘点</el-button>
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

    <!-- 新建盘点单 -->
    <el-dialog v-model="dialogVisible" title="新建盘点单" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="form.warehouseId" placeholder="请选择" style="width:100%;">
            <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- 盘点详情/录入 -->
    <el-dialog v-model="detailVisible" :title="detailOrder.status === 0 ? '盘点录入' : '盘点详情'" width="850px">
      <el-descriptions :column="3" border style="margin-bottom: 16px;">
        <el-descriptions-item label="单据编号">{{ detailOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ getWarehouseName(detailOrder.warehouseId) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailOrder.status === 0 ? 'warning' : 'success'">{{ detailOrder.status === 0 ? '盘点中' : '已完成' }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-table :data="detailItems" border size="small">
        <el-table-column prop="materialId" label="物资">
          <template #default="{ row }">{{ getMaterialName(row.materialId) }}</template>
        </el-table-column>
        <el-table-column prop="systemQuantity" label="系统数量" width="100" align="right" />
        <el-table-column label="实际数量" width="140">
          <template #default="{ row }">
            <el-input-number v-if="detailOrder.status === 0" v-model="row.actualQuantity" :min="0" size="small" style="width:100%;" @change="updateItem(row)" />
            <span v-else>{{ row.actualQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="diffQuantity" label="差异" width="80" align="right">
          <template #default="{ row }">
            <span :style="{ color: row.diffQuantity > 0 ? '#67c23a' : row.diffQuantity < 0 ? '#f56c6c' : '' }">
              {{ row.diffQuantity > 0 ? '+' : '' }}{{ row.diffQuantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" width="160">
          <template #default="{ row }">
            <el-input v-if="detailOrder.status === 0" v-model="row.remark" size="small" @change="updateItem(row)" />
            <span v-else>{{ row.remark }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getStocktakeOrderList, createStocktakeOrder, completeStocktakeOrder,
  deleteStocktakeOrder, getStocktakeItems, updateStocktakeItem, getAllWarehouses, getAllMaterials
} from '@/api'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const formRef = ref()
const tableData = ref([])
const total = ref(0)
const warehouses = ref([])
const materials = ref([])
const detailOrder = reactive({})
const detailItems = ref([])

const query = reactive({ orderNo: '', status: null, current: 1, size: 10 })
const form = reactive({ warehouseId: null, remark: '' })

const rules = {
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
}

const getWarehouseName = (id) => warehouses.value.find((w) => w.id === id)?.name || id
const getMaterialName = (id) => materials.value.find((m) => m.id === id)?.name || id

const loadData = async () => {
  loading.value = true
  try {
    const res = await getStocktakeOrderList(query)
    if (res.data.code === 200) { tableData.value = res.data.data.records; total.value = res.data.data.total }
  } finally { loading.value = false }
}

const handleSearch = () => { query.current = 1; loadData() }
const resetQuery = () => { Object.assign(query, { orderNo: '', status: null, current: 1 }); loadData() }

const openDialog = () => {
  Object.assign(form, { warehouseId: null, remark: '' })
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const res = await createStocktakeOrder(form)
      if (res.data.code === 200) { ElMessage.success('创建成功'); dialogVisible.value = false; loadData() }
    } finally { submitting.value = false }
  })
}

const viewDetail = async (row) => {
  Object.assign(detailOrder, row)
  const res = await getStocktakeItems(row.id)
  if (res.data.code === 200) detailItems.value = res.data.data
  detailVisible.value = true
}

const updateItem = async (row) => {
  row.diffQuantity = (row.actualQuantity || 0) - (row.systemQuantity || 0)
  await updateStocktakeItem(row.id, { actualQuantity: row.actualQuantity, remark: row.remark })
}

const handleComplete = (id) => {
  ElMessageBox.confirm('确认完成盘点？差异将自动调整库存。', '提示', { type: 'warning' }).then(async () => {
    const res = await completeStocktakeOrder(id)
    if (res.data.code === 200) { ElMessage.success('盘点完成'); loadData() }
  }).catch(() => {})
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该盘点单？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteStocktakeOrder(id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
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
</style>
