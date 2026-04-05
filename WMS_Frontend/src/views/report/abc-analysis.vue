<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card v-for="item in summaryCards" :key="item.label" style="margin-bottom: 12px;">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <div style="font-size: 14px; color: #999;">{{ item.label }}</div>
              <div style="font-size: 28px; font-weight: bold; margin-top: 4px;" :style="{ color: item.color }">
                {{ item.count }}
              </div>
            </div>
            <div style="font-size: 13px; color: #666;">
              占比 {{ item.percent }}%
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card header="ABC分类帕累托图">
          <div ref="chartRef" style="height: 380px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 12px;" header="ABC分类明细">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="materialCode" label="物资编码" width="120" />
        <el-table-column prop="materialName" label="物资名称" width="160" />
        <el-table-column prop="quantity" label="库存数量" width="100" align="right" />
        <el-table-column prop="avgPrice" label="均价" width="100" align="right" />
        <el-table-column prop="totalValue" label="库存价值" width="120" align="right" />
        <el-table-column label="价值占比" width="100" align="right">
          <template #default="{ row }">{{ row.valuePercent?.toFixed(1) }}%</template>
        </el-table-column>
        <el-table-column label="累计占比" width="100" align="right">
          <template #default="{ row }">{{ row.cumPercent?.toFixed(1) }}%</template>
        </el-table-column>
        <el-table-column prop="classification" label="分类" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.classification === 'A' ? 'danger' : row.classification === 'B' ? 'warning' : 'info'">
              {{ row.classification }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { getAbcAnalysis } from '@/api'

const chartRef = ref()
let chart = null
const loading = ref(false)
const tableData = ref([])

const summaryCards = computed(() => {
  const a = tableData.value.filter(r => r.classification === 'A')
  const b = tableData.value.filter(r => r.classification === 'B')
  const c = tableData.value.filter(r => r.classification === 'C')
  const total = tableData.value.length || 1
  return [
    { label: 'A类 (重点管控)', count: a.length, percent: (a.length / total * 100).toFixed(1), color: '#f56c6c' },
    { label: 'B类 (常规管理)', count: b.length, percent: (b.length / total * 100).toFixed(1), color: '#e6a23c' },
    { label: 'C类 (简化管理)', count: c.length, percent: (c.length / total * 100).toFixed(1), color: '#909399' },
  ]
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAbcAnalysis()
    if (res.code === 200) {
      tableData.value = res.data || []
      renderChart()
    }
  } finally { loading.value = false }
}

const renderChart = () => {
  if (!chart) chart = echarts.init(chartRef.value)
  const names = tableData.value.map(r => r.materialName)
  const values = tableData.value.map(r => r.totalValue)
  const cumPercents = tableData.value.map(r => r.cumPercent)
  const colors = tableData.value.map(r =>
    r.classification === 'A' ? '#f56c6c' : r.classification === 'B' ? '#e6a23c' : '#c0c4cc'
  )

  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
    legend: { data: ['库存价值', '累计占比'] },
    grid: { left: 60, right: 60, top: 40, bottom: 80 },
    xAxis: {
      type: 'category', data: names,
      axisLabel: { rotate: 30, fontSize: 11, interval: 0 },
    },
    yAxis: [
      { type: 'value', name: '库存价值' },
      { type: 'value', name: '累计占比(%)', max: 100, position: 'right' },
    ],
    series: [
      {
        name: '库存价值', type: 'bar', data: values,
        itemStyle: { color: (params) => colors[params.dataIndex] },
      },
      {
        name: '累计占比', type: 'line', yAxisIndex: 1, smooth: true,
        data: cumPercents, itemStyle: { color: '#409eff' }, lineStyle: { width: 2 },
        markLine: {
          silent: true, lineStyle: { type: 'dashed' },
          data: [
            { yAxis: 80, label: { formatter: '80%' } },
            { yAxis: 95, label: { formatter: '95%' } },
          ],
        },
      },
    ],
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
