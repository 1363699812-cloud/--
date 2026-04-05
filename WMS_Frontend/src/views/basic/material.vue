<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="物资名称">
          <el-input v-model="query.name" placeholder="请输入物资名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="物资编码">
          <el-input v-model="query.code" placeholder="请输入物资编码" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.categoryId" placeholder="全部" clearable>
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 12px;">
      <div class="toolbar">
        <el-button type="primary" @click="openDialog()">新增物资</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="code" label="物资编码" width="120" />
        <el-table-column prop="name" label="物资名称" width="150" />
        <el-table-column prop="categoryId" label="分类" width="120">
          <template #default="{ row }">{{ getCategoryName(row.categoryId) }}</template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="70" />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="safetyStock" label="安全库存" width="90" />
        <el-table-column prop="minStock" label="最低库存" width="90" />
        <el-table-column prop="maxStock" label="最高库存" width="90" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggleStatus(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
            <el-button link type="success" @click="viewInventory(row)">查看库存</el-button>
            <el-button link type="danger" v-if="row.status === 0" @click="handleDelete(row.id)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑物资' : '新增物资'" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="物资编码" prop="code"><el-input v-model="form.code" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物资名称" prop="name"><el-input v-model="form.name" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择" style="width:100%;">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit"><el-input v-model="form.unit" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="规格"><el-input v-model="form.specification" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="安全库存">
            <el-input-number v-model="form.safetyStock" :min="0" controls-position="right" style="width:100%;" />
            <div style="color: #909399; font-size: 12px; line-height: 1.4; margin-top: 2px;">低于此值触发预警</div>
          </el-form-item></el-col>
          <el-col :span="8"><el-form-item label="最低库存">
            <el-input-number v-model="form.minStock" :min="0" controls-position="right" style="width:100%;" />
            <div style="color: #909399; font-size: 12px; line-height: 1.4; margin-top: 2px;">库存下限</div>
          </el-form-item></el-col>
          <el-col :span="8"><el-form-item label="最高库存">
            <el-input-number v-model="form.maxStock" :min="0" controls-position="right" style="width:100%;" />
            <div style="color: #909399; font-size: 12px; line-height: 1.4; margin-top: 2px;">库存上限</div>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看库存弹窗 -->
    <el-dialog v-model="inventoryVisible" title="物资库存分布" width="650px" destroy-on-close>
      <el-descriptions :column="2" border size="small" style="margin-bottom: 16px;">
        <el-descriptions-item label="物资编码">{{ currentMaterial.code }}</el-descriptions-item>
        <el-descriptions-item label="物资名称">{{ currentMaterial.name }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="inventoryData" v-loading="inventoryLoading" border size="small">
        <el-table-column prop="warehouseName" label="仓库" />
        <el-table-column prop="quantity" label="当前库存" width="100" />
        <el-table-column prop="lockQuantity" label="锁定数量" width="100" />
        <el-table-column label="可用库存" width="100">
          <template #default="{ row }">{{ row.quantity - (row.lockQuantity || 0) }}</template>
        </el-table-column>
      </el-table>
      <div v-if="inventoryData.length === 0 && !inventoryLoading" style="text-align: center; padding: 20px; color: #999;">
        该物资暂无库存记录
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMaterialList, saveMaterial, updateMaterial, deleteMaterial, getAllCategories, getInventoryList, getAllWarehouses } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const inventoryVisible = ref(false)
const inventoryLoading = ref(false)
const formRef = ref()
const tableData = ref([])
const total = ref(0)
const categories = ref([])
const inventoryData = ref([])
const currentMaterial = reactive({ code: '', name: '' })
const warehouseMap = ref({})

const query = reactive({ name: '', code: '', categoryId: null, current: 1, size: 10 })
const form = reactive({ id: null, code: '', name: '', categoryId: null, unit: '', specification: '', safetyStock: 0, minStock: 0, maxStock: 0, description: '', status: 1 })

const rules = {
  code: [{ required: true, message: '请输入物资编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入物资名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
}

const getCategoryName = (id) => categories.value.find((c) => c.id === id)?.name || id

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMaterialList(query)
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } finally { loading.value = false }
}

const handleSearch = () => { query.current = 1; loadData() }
const resetQuery = () => { Object.assign(query, { name: '', code: '', categoryId: null, current: 1 }); loadData() }

const openDialog = (row) => {
  if (row) Object.assign(form, { ...row })
  else Object.assign(form, { id: null, code: '', name: '', categoryId: null, unit: '', specification: '', safetyStock: 0, minStock: 0, maxStock: 0, description: '', status: 1 })
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    // 库存字段交叉验证
    if (form.minStock > form.safetyStock) {
      ElMessage.warning('最低库存不能大于安全库存')
      return
    }
    if (form.safetyStock > form.maxStock) {
      ElMessage.warning('安全库存不能大于最高库存')
      return
    }
    submitting.value = true
    try {
      const res = form.id ? await updateMaterial(form.id, form) : await saveMaterial(form)
      if (res.code === 200) { ElMessage.success(form.id ? '更新成功' : '新增成功'); dialogVisible.value = false; loadData() }
    } finally { submitting.value = false }
  })
}

const viewInventory = async (row) => {
  currentMaterial.code = row.code
  currentMaterial.name = row.name
  inventoryVisible.value = true
  inventoryLoading.value = true
  try {
    const res = await getInventoryList({ materialId: row.id, size: 100 })
    if (res.code === 200) {
      inventoryData.value = (res.data.records || []).map(item => ({
        ...item,
        warehouseName: warehouseMap.value[item.warehouseId] || item.warehouseId
      }))
    }
  } finally { inventoryLoading.value = false }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该物资？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteMaterial(id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadData() }
  }).catch(() => {})
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const res = await updateMaterial(row.id, { status: newStatus })
  if (res.code === 200) { ElMessage.success(newStatus === 1 ? '已启用' : '已停用'); loadData() }
}

onMounted(async () => {
  const res = await getAllCategories()
  if (res.code === 200) categories.value = res.data
  const wRes = await getAllWarehouses()
  if (wRes.code === 200) {
    wRes.data.forEach(w => { warehouseMap.value[w.id] = w.name })
  }
  loadData()
})
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
</style>
