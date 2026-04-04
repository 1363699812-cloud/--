<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="操作人">
          <el-input v-model="query.username" placeholder="请输入操作人" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="模块">
          <el-input v-model="query.module" placeholder="请输入模块名" clearable @keyup.enter="handleSearch" />
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
        <el-table-column prop="createTime" label="操作时间" width="170" />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="operation" label="操作" width="180" />
        <el-table-column prop="method" label="方法" width="250" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="params" label="参数" show-overflow-tooltip />
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
import { ref, reactive, watch, onMounted } from 'vue'
import { getOperationLogList } from '@/api'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dateRange = ref(null)

const query = reactive({ username: '', module: '', startDate: null, endDate: null, current: 1, size: 10 })

watch(dateRange, (val) => {
  query.startDate = val?.[0] || null
  query.endDate = val?.[1] || null
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getOperationLogList(query)
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } finally { loading.value = false }
}

const handleSearch = () => { query.current = 1; loadData() }
const resetQuery = () => {
  Object.assign(query, { username: '', module: '', startDate: null, endDate: null, current: 1 })
  dateRange.value = null
  loadData()
}

onMounted(() => loadData())
</script>
