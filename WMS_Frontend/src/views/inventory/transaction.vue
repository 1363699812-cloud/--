<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="仓库">
          <el-select v-model="query.warehouseId" placeholder="全部" clearable>
            <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="物资">
          <el-select v-model="query.materialId" placeholder="全部" clearable filterable>
            <el-option v-for="m in materials" :key="m.id" :label="`${m.code} - ${m.name}`" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="变动类型">
          <el-select v-model="query.changeType" placeholder="全部" clearable>
            <el-option label="入库" value="IN" />
            <el-option label="出库" value="OUT" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源类型">
          <el-select v-model="query.referenceType" placeholder="全部" clearable>
            <el-option label="入库" value="INBOUND" />
            <el-option label="出库" value="OUTBOUND" />
            <el-option label="调拨入" value="TRANSFER_IN" />
            <el-option label="调拨出" value="TRANSFER_OUT" />
            <el-option label="报损" value="DAMAGE" />
            <el-option label="报废" value="SCRAP" />
            <el-option label="盘点" value="STOCKTAKE" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 12px;">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column prop="warehouseId" label="仓库" width="130">
          <template #default="{ row }">{{ getWarehouseName(row.warehouseId) }}</template>
        </el-table-column>
        <el-table-column prop="materialId" label="物资" width="160">
          <template #default="{ row }">{{ getMaterialName(row.materialId) }}</template>
        </el-table-column>
        <el-table-column prop="changeType" label="变动类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.changeType === 'IN' ? 'success' : 'danger'">{{ row.changeType === 'IN' ? '入库' : '出库' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="变动数量" width="100" align="right" />
        <el-table-column prop="referenceType" label="来源类型" width="100">
          <template #default="{ row }">{{ refTypeMap[row.referenceType] || row.referenceType }}</template>
        </el-table-column>
        <el-table-column prop="referenceId" label="来源单号" width="100" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createUser" label="操作人" width="100" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getTransactionList, getAllWarehouses, getAllMaterials } from '@/api'

const route = useRoute()
const refTypeMap = { INBOUND: '入库', OUTBOUND: '出库', TRANSFER_IN: '调拨入', TRANSFER_OUT: '调拨出', DAMAGE: '报损', SCRAP: '报废', STOCKTAKE: '盘点' }

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const warehouses = ref([])
const materials = ref([])
const dateRange = ref(null)

const query = reactive({ warehouseId: null, materialId: null, changeType: null, referenceType: null, startDate: null, endDate: null, current: 1, size: 10 })

const getWarehouseName = (id) => warehouses.value.find((w) => w.id === id)?.name || id
const getMaterialName = (id) => materials.value.find((m) => m.id === id)?.name || id

watch(dateRange, (val) => {
  query.startDate = val?.[0] || null
  query.endDate = val?.[1] || null
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTransactionList(query)
    if (res.data.code === 200) { tableData.value = res.data.data.records; total.value = res.data.data.total }
  } finally { loading.value = false }
}

const handleSearch = () => { query.current = 1; loadData() }
const resetQuery = () => {
  Object.assign(query, { warehouseId: null, materialId: null, changeType: null, referenceType: null, startDate: null, endDate: null, current: 1 })
  dateRange.value = null
  loadData()
}

onMounted(async () => {
  const [w, m] = await Promise.all([getAllWarehouses(), getAllMaterials()])
  if (w.data.code === 200) warehouses.value = w.data.data
  if (m.data.code === 200) materials.value = m.data.data
  // 从库存详情跳转过来时, 自动填充筛选条件
  if (route.query.warehouseId) query.warehouseId = Number(route.query.warehouseId)
  if (route.query.materialId) query.materialId = Number(route.query.materialId)
  loadData()
})
</script>
