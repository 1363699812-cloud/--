<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="item in statCards" :key="item.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-body">
            <div class="stat-info">
              <div class="stat-label">{{ item.label }}</div>
              <div class="stat-value">{{ item.value }}</div>
            </div>
            <el-icon :size="48" :color="item.color"><component :is="item.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card header="近7天出入库趋势">
          <div ref="trendChartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card header="快捷信息">
          <div class="quick-info">
            <div class="info-item">
              <span class="info-label">待审核入库单</span>
              <el-tag type="warning">{{ dashboard.pendingInbound || 0 }}</el-tag>
            </div>
            <div class="info-item">
              <span class="info-label">待审核出库单</span>
              <el-tag type="warning">{{ dashboard.pendingOutbound || 0 }}</el-tag>
            </div>
            <div class="info-item">
              <span class="info-label">库存预警数</span>
              <el-tag type="danger">{{ dashboard.alertCount || 0 }}</el-tag>
            </div>
            <div class="info-item">
              <span class="info-label">今日入库量</span>
              <el-tag type="success">{{ dashboard.todayInbound || 0 }}</el-tag>
            </div>
            <div class="info-item">
              <span class="info-label">今日出库量</span>
              <el-tag type="info">{{ dashboard.todayOutbound || 0 }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { getDashboard } from '@/api'

const trendChartRef = ref()
let trendChart = null
const dashboard = reactive({})

const statCards = ref([
  { label: '物资总数', value: 0, icon: 'Box', color: '#409eff' },
  { label: '仓库总数', value: 0, icon: 'House', color: '#67c23a' },
  { label: '库存总量', value: 0, icon: 'Coin', color: '#e6a23c' },
  { label: '预警数量', value: 0, icon: 'Bell', color: '#f56c6c' },
])

const loadData = async () => {
  const res = await getDashboard()
  if (res.code === 200) {
    const d = res.data
    Object.assign(dashboard, d)
    statCards.value[0].value = d.materialCount || 0
    statCards.value[1].value = d.warehouseCount || 0
    statCards.value[2].value = d.totalInventory || 0
    statCards.value[3].value = d.alertCount || 0
    renderTrendChart(d.trends || [])
  }
}

const renderTrendChart = (trends) => {
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  const dates = trends.map((t) => t.date)
  const inData = trends.map((t) => t.inbound || 0)
  const outData = trends.map((t) => t.outbound || 0)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['入库', '出库'] },
    grid: { left: 50, right: 30, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [
      { name: '入库', type: 'line', smooth: true, data: inData, itemStyle: { color: '#67c23a' } },
      { name: '出库', type: 'line', smooth: true, data: outData, itemStyle: { color: '#f56c6c' } },
    ],
  })
}

const handleResize = () => trendChart?.resize()

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})
onBeforeUnmount(() => {
  trendChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.stat-card-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-label { color: #909399; font-size: 14px; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; margin-top: 8px; }
.quick-info .info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.quick-info .info-item:last-child { border-bottom: none; }
.info-label { color: #606266; }
</style>
