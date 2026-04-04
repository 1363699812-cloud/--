<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>库存预警列表</span>
          <el-button type="primary" @click="loadData" :loading="loading">刷新</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="warehouseName" label="仓库" width="150" />
        <el-table-column prop="materialCode" label="物资编码" width="120" />
        <el-table-column prop="materialName" label="物资名称" width="180" />
        <el-table-column prop="quantity" label="当前库存" width="100" align="right" />
        <el-table-column prop="safetyStock" label="安全库存" width="100" align="right" />
        <el-table-column prop="maxStock" label="最高库存" width="100" align="right" />
        <el-table-column label="预警类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="danger" v-if="row.quantity < row.safetyStock">低于安全库存</el-tag>
            <el-tag type="warning" v-else-if="row.quantity > row.maxStock">超过最高库存</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="差异量" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.quantity < row.safetyStock" style="color:#f56c6c;">{{ row.quantity - row.safetyStock }}</span>
            <span v-else-if="row.quantity > row.maxStock" style="color:#e6a23c;">+{{ row.quantity - row.maxStock }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStockAlerts } from '@/api'

const loading = ref(false)
const tableData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getStockAlerts()
    if (res.data.code === 200) tableData.value = res.data.data
  } finally { loading.value = false }
}

onMounted(() => loadData())
</script>
