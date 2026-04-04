<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="仓库名称">
          <el-input v-model="query.name" placeholder="请输入仓库名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="仓库编码">
          <el-input v-model="query.code" placeholder="请输入仓库编码" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 + 表格 -->
    <el-card style="margin-top: 12px;">
      <div class="toolbar">
        <el-button type="primary" @click="openDialog()">新增仓库</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%;">
        <el-table-column prop="code" label="仓库编码" width="120" />
        <el-table-column prop="name" label="仓库名称" width="150" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="capacity" label="容量" width="80" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'">{{ row.status === 'active' ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑仓库' : '新增仓库'" width="550px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="仓库编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入仓库编码" />
        </el-form-item>
        <el-form-item label="仓库名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入仓库名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="form.capacity" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="active">启用</el-radio>
            <el-radio value="inactive">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWarehouseList, saveWarehouse, updateWarehouse, deleteWarehouse } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const tableData = ref([])
const total = ref(0)

const query = reactive({ name: '', code: '', current: 1, size: 10 })
const form = reactive({ id: null, code: '', name: '', address: '', contactPerson: '', phone: '', capacity: 0, description: '', status: 'active' })

const rules = {
  code: [{ required: true, message: '请输入仓库编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }],
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getWarehouseList(query)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { query.current = 1; loadData() }
const resetQuery = () => { Object.assign(query, { name: '', code: '', current: 1 }); loadData() }

const openDialog = (row) => {
  if (row) {
    Object.assign(form, { ...row })
  } else {
    Object.assign(form, { id: null, code: '', name: '', address: '', contactPerson: '', phone: '', capacity: 0, description: '', status: 'active' })
  }
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const res = form.id ? await updateWarehouse(form.id, form) : await saveWarehouse(form)
      if (res.code === 200) {
        ElMessage.success(form.id ? '更新成功' : '新增成功')
        dialogVisible.value = false
        loadData()
      }
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该仓库？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteWarehouse(id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadData() }
  }).catch(() => {})
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 0; }
.toolbar { margin-bottom: 12px; }
</style>
