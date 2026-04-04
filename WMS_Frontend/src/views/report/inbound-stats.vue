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
        <el-card header="入库趋势">
          <div ref="trendRef" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 12px;" header="入库明细">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="orderCount" label="入库单数" width="100" align="right" />
        <el-table-column prop="totalQuantity" label="入库数量" width="120" align="right" />
        <el-table-column prop="totalAmount" label="入库金额" width="130" align="right">
          <template #default="{ row }">{{ row.totalAmount?.toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { getInboundStats } from '@/api'

const trendRef = ref()
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
    const res = await getInboundStats(query.value)
    if (res.code === 200) {
      tableData.value = res.data || []
      renderChart()
    }
  } finally { loading.value = false }
}

const renderChart = () => {
  if (!chart) chart = echarts.init(trendRef.value)
  const dates = tableData.value.map((r) => r.date)
  const quantities = tableData.value.map((r) => r.totalQuantity || 0)
  const amounts = tableData.value.map((r) => r.totalAmount || 0)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['入库数量', '入库金额'] },
    grid: { left: 60, right: 60, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: dates },
    yAxis: [
      { type: 'value', name: '数量' },
      { type: 'value', name: '金额', position: 'right' },
    ],
    series: [
      { name: '入库数量', type: 'bar', data: quantities, itemStyle: { color: '#67c23a' } },
      { name: '入库金额', type: 'line', yAxisIndex: 1, smooth: true, data: amounts, itemStyle: { color: '#409eff' } },
    ],
  })
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', () => chart?.resize())
})
onBeforeUnmount(() => {
  chart?.dispose()
  window.removeEventListener('resize', () => chart?.resize())
})
</script>
