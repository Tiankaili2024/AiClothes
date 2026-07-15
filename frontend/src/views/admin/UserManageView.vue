<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span><el-icon><User /></el-icon> 用户管理</span>
        <el-input v-model="keyword" placeholder="搜索用户名/昵称" style="width:250px" clearable @change="search" />
      </div>
    </template>
    <el-table :data="users" stripe style="width:100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="gender" label="性别" width="60">
        <template #default="{ row }">{{ {0:'未知',1:'男',2:'女'}[row.gender] }}</template>
      </el-table-column>
      <el-table-column prop="city" label="城市" width="100" />
      <el-table-column prop="role" label="角色" width="80">
        <template #default="{ row }"><el-tag :type="row.role===1?'danger':'info'">{{ row.role===1?'管理员':'用户' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status===1?'success':'danger'">{{ row.status===1?'正常':'禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-switch
            v-if="row.role !== 1"
            :model-value="row.status === 1"
            @change="toggleStatus(row)"
            active-text="正常" inactive-text="禁用"
          />
        </template>
      </el-table-column>
    </el-table>
    <div style="text-align:center;margin-top:20px">
      <el-pagination
        v-model:current-page="page" :page-size="size" :total="total"
        layout="prev, pager, next" @current-change="loadUsers"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listUsers, toggleUserStatus } from '../../api/admin'

const users = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')

onMounted(() => loadUsers())

async function loadUsers() {
  try {
    const res = await listUsers(page.value, size.value, keyword.value)
    if (res.code === 200) {
      users.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {}
}

function search() {
  page.value = 1
  loadUsers()
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await toggleUserStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  } catch (e) {}
}
</script>
