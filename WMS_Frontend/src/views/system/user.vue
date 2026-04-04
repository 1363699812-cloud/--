<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="请输入用户名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.role" placeholder="全部" clearable>
            <el-option label="管理员" value="admin" />
            <el-option label="仓库管理员" value="warehouse_keeper" />
            <el-option label="采购/销售" value="purchaser_seller" />
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
        <el-button type="primary" @click="openDialog()">新增用户</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="role" label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="roleMap[row.role]?.type">{{ roleMap[row.role]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="550px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="密码" :prop="form.id ? '' : 'password'" v-if="!form.id">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%;">
            <el-option label="管理员" value="admin" />
            <el-option label="仓库管理员" value="warehouse_keeper" />
            <el-option label="采购/销售" value="purchaser_seller" />
          </el-select>
        </el-form-item>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, saveUser, updateUser, deleteUser } from '@/api'

const roleMap = {
  admin: { label: '管理员', type: 'danger' },
  warehouse_keeper: { label: '仓库管理员', type: '' },
  purchaser_seller: { label: '采购/销售', type: 'success' },
}

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const tableData = ref([])
const total = ref(0)

const query = reactive({ username: '', role: null, current: 1, size: 10 })
const form = reactive({ id: null, username: '', password: '', realName: '', phone: '', email: '', role: 'warehouse_keeper', status: 1 })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList(query)
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } finally { loading.value = false }
}

const handleSearch = () => { query.current = 1; loadData() }
const resetQuery = () => { Object.assign(query, { username: '', role: null, current: 1 }); loadData() }

const openDialog = (row) => {
  if (row) Object.assign(form, { ...row, password: '' })
  else Object.assign(form, { id: null, username: '', password: '', realName: '', phone: '', email: '', role: 'warehouse_keeper', status: 1 })
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const res = form.id ? await updateUser(form.id, form) : await saveUser(form)
      if (res.code === 200) { ElMessage.success(form.id ? '更新成功' : '新增成功'); dialogVisible.value = false; loadData() }
    } finally { submitting.value = false }
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该用户？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteUser(id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadData() }
  }).catch(() => {})
}

onMounted(() => loadData())
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
</style>
