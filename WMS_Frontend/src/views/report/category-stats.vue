<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card header="物资分类分布（饼图）">
          <div ref="pieRef" style="height: 400px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="分类库存数量（柱状图）">
          <div ref="barRef" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 12px;" header="分类统计明细">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="categoryName" label="分类名称" width="180" />
        <el-table-column prop="materialCount" label="物资种类数" width="120" align="right" />
        <el-table-column prop="totalQuantity" label="库存总量" width="120" align="right" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { getCategoryStats } from '@/api'

const pieRef = ref()
const barRef = ref()
let pieChart = null
let barChart = null
const loading = ref(false)
const tableData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCategoryStats()
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
      renderCharts()
    }
  } finally { loading.value = false }
}

const renderCharts = () => {
  if (!pieChart) pieChart = echarts.init(pieRef.value)
  if (!barChart) barChart = echarts.init(barRef.value)

  const names = tableData.value.map((r) => r.categoryName)
  const pieData = tableData.value.map((r) => ({ name: r.categoryName, value: r.materialCount }))
  const barData = tableData.value.map((r) => r.totalQuantity || 0)

  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie', radius: ['40%', '70%'], avoidLabelOverlap: true,
      label: { show: true, formatter: '{b}\n{d}%' },
      data: pieData,
    }],
  })

  barChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 30, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: names, axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', name: '库存总量' },
    series: [{ type: 'bar', data: barData, itemStyle: { color: '#409eff' } }],
  })
}

const handleResize = () => { pieChart?.resize(); barChart?.resize() }

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})
onBeforeUnmount(() => {
  pieChart?.dispose()
  barChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>
