<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="时间范围">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" style="margin-top: 12px;">
      <el-col :span="24">
        <el-card header="库存周转率排行">
          <div ref="chartRef" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 12px;" header="周转率明细">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="materialCode" label="物资编码" width="120" />
        <el-table-column prop="materialName" label="物资名称" width="160" />
        <el-table-column prop="currentStock" label="当前库存" width="100" align="right" />
        <el-table-column prop="outQuantity" label="期间出库量" width="120" align="right" />
        <el-table-column prop="turnoverRate" label="年周转率" width="120" align="right">
          <template #default="{ row }">
            <span :style="{ color: row.turnoverRate >= 6 ? '#67c23a' : row.turnoverRate >= 2 ? '#e6a23c' : '#f56c6c' }">
              {{ row.turnoverRate }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="turnoverDays" label="周转天数" width="120" align="right">
          <template #default="{ row }">
            <span>{{ row.turnoverDays >= 999 ? '-' : row.turnoverDays + '天' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="周转状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.turnoverRate >= 6 ? 'success' : row.turnoverRate >= 2 ? 'warning' : 'danger'">
              {{ row.turnoverRate >= 6 ? '良好' : row.turnoverRate >= 2 ? '一般' : '滞销' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { getTurnoverRate } from '@/api'

const chartRef = ref()
let chart = null
const loading = ref(false)
const tableData = ref([])
const dateRange = ref(null)
const query = ref({ startDate: null, endDate: null })

watch(dateRange, (val) => {
  query.value.startDate = val?.[0] || null
  query.value.endDate = val?.[1] || null
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTurnoverRate(query.value)
    if (res.code === 200) {
      tableData.value = res.data || []
      renderChart()
    }
  } finally { loading.value = false }
}

const renderChart = () => {
  if (!chart) chart = echarts.init(chartRef.value)
  const top20 = tableData.value.slice(0, 20)
  const names = top20.map(r => r.materialName)
  const rates = top20.map(r => r.turnoverRate)
  const colors = top20.map(r =>
    r.turnoverRate >= 6 ? '#67c23a' : r.turnoverRate >= 2 ? '#e6a23c' : '#f56c6c'
  )

  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 30, top: 30, bottom: 80 },
    xAxis: {
      type: 'category', data: names,
      axisLabel: { rotate: 30, fontSize: 11, interval: 0 },
    },
    yAxis: { type: 'value', name: '年周转率' },
    series: [{
      type: 'bar', data: rates,
      itemStyle: { color: (params) => colors[params.dataIndex] },
      label: { show: true, position: 'top', fontSize: 11 },
    }],
  })
}

const handleResize = () => chart?.resize()

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})
onBeforeUnmount(() => {
  chart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>
