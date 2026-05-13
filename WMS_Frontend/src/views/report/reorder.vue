<template>
  <div class="page-container">
    <!-- 汇总卡片 -->
    <el-row :gutter="20" style="margin-bottom: 16px;">
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #999;">需补货物资数</div>
            <div style="font-size: 32px; font-weight: bold; color: #f56c6c; margin-top: 8px;">
              {{ tableData.length }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #999;">严重缺货</div>
            <div style="font-size: 32px; font-weight: bold; color: #f56c6c; margin-top: 8px;">
              {{ tableData.filter(r => r.urgency === '严重缺货').length }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #999;">紧急补货</div>
            <div style="font-size: 32px; font-weight: bold; color: #e6a23c; margin-top: 8px;">
              {{ tableData.filter(r => r.urgency === '紧急补货').length }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #999;">建议补货</div>
            <div style="font-size: 32px; font-weight: bold; color: #409eff; margin-top: 8px;">
              {{ tableData.filter(r => r.urgency === '建议补货').length }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 补货建议表格 -->
    <el-card header="补货建议明细">
      <template #extra>
        <el-button type="primary" size="small" @click="loadData" :loading="loading">刷新数据</el-button>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe border max-height="520">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="materialCode" label="物资编码" width="120" />
        <el-table-column prop="materialName" label="物资名称" width="150" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="unit" label="单位" width="60" />
        <el-table-column prop="currentStock" label="当前库存" width="100" align="right" />
        <el-table-column prop="safetyStock" label="安全库存" width="100" align="right" />
        <el-table-column prop="reorderPoint" label="补货点(ROP)" width="110" align="right" />
        <el-table-column prop="shortage" label="缺口数量" width="100" align="right">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">{{ row.shortage }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="eoq" label="建议补货量(EOQ)" width="130" align="right">
          <template #default="{ row }">
            <el-tag type="success">{{ row.eoq }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxStock" label="最大库存" width="100" align="right" />
        <el-table-column prop="urgency" label="紧急程度" width="110" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.urgency === '严重缺货' ? 'danger' : row.urgency === '紧急补货' ? 'warning' : 'primary'">
              {{ row.urgency }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" description="暂无需要补货的物资" />
    </el-card>

    <!-- 提示说明 -->
    <el-card style="margin-top: 12px;">
      <template #header>
        <span>补货机制说明</span>
      </template>
      <div style="font-size: 13px; color: #666; line-height: 2;">
        <p><strong>补货点 (ROP)</strong> = 日均需求量 &times; 补货提前期 + 安全库存</p>
        <p><strong>经济订货批量 (EOQ)</strong> = &radic;(2 &times; 年需求量 &times; 单次订货成本 / 单位年持有成本)</p>
        <p><strong>触发条件</strong>: 当前库存 &le; 补货点 (ROP) 时，系统自动生成补货建议</p>
        <p><strong>建议补货量</strong>: 取 EOQ 与 (ROP - 当前库存 + 安全库存) 中的较大值</p>
        <p style="margin-top: 8px; color: #999;">
          <em>注：日均需求量基于最近90天已完成出库数据统计；提前期默认7天，可在物资管理中设置。</em>
        </p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getReorderSuggestions } from '@/api'

const loading = ref(false)
const tableData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getReorderSuggestions()
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } finally { loading.value = false }
}

onMounted(() => { loadData() })
</script>
