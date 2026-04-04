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
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 12px;">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="warehouseId" label="仓库" width="150">
          <template #default="{ row }">{{ getWarehouseName(row.warehouseId) }}</template>
        </el-table-column>
        <el-table-column prop="materialId" label="物资" width="180">
          <template #default="{ row }">{{ getMaterialName(row.materialId) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="当前库存" width="120" align="right" />
        <el-table-column prop="lockQuantity" label="锁定数量" width="120" align="right" />
        <el-table-column label="可用库存" width="120" align="right">
          <template #default="{ row }">{{ (row.quantity || 0) - (row.lockQuantity || 0) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getInventoryList, getAllWarehouses, getAllMaterials } from '@/api'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const warehouses = ref([])
const materials = ref([])

const query = reactive({ warehouseId: null, materialId: null, current: 1, size: 10 })

const getWarehouseName = (id) => warehouses.value.find((w) => w.id === id)?.name || id
const getMaterialName = (id) => materials.value.find((m) => m.id === id)?.name || id

const loadData = async () => {
  loading.value = true
  try {
    const res = await getInventoryList(query)
    if (res.data.code === 200) { tableData.value = res.data.data.records; total.value = res.data.data.total }
  } finally { loading.value = false }
}

const handleSearch = () => { query.current = 1; loadData() }
const resetQuery = () => { Object.assign(query, { warehouseId: null, materialId: null, current: 1 }); loadData() }

const viewDetail = (row) => {
  router.push({ path: '/inventory/transaction', query: { warehouseId: row.warehouseId, materialId: row.materialId } })
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
