<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:8px">
        <span><el-icon><User /></el-icon> 用户管理</span>
        <el-input v-model="keyword" placeholder="搜索用户名/昵称" style="width:250px" clearable @change="search" />
      </div>
    </template>
    <el-skeleton :loading="loading && !users.length" animated>
      <template #template>
        <div v-for="i in 4" :key="i" style="display:flex;gap:12px;padding:14px 0;border-bottom:1px solid #eee">
          <el-skeleton-item variant="text" style="width:60px" />
          <el-skeleton-item variant="text" style="width:80px" />
          <el-skeleton-item variant="text" style="width:60px" />
          <el-skeleton-item variant="text" style="width:100px;flex:1" />
          <el-skeleton-item variant="text" style="width:80px" />
          <el-skeleton-item variant="text" style="width:120px" />
        </div>
      </template>
      <template #default>
        <el-empty v-if="!users.length && !loading" description="暂无用户数据" :image-size="80" />
        <el-table v-else :data="users" stripe style="width:100%" v-loading="loading" element-loading-text="加载中...">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column label="性别" width="60">
            <template #default="{ row }">
              {{ {'男':'男','女':'女',1:'男',2:'女',0:'未知'}[row.gender] || '未知' }}
            </template>
          </el-table-column>
          <el-table-column prop="city" label="城市" width="100" />
          <el-table-column label="角色" width="80">
            <template #default="{ row }">
              <el-tag :type="(row.role===1||row.role==='admin')?'danger':'info'" size="small">
                {{ (row.role===1||row.role==='admin')?'管理员':'用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="(row.status===1||row.status==='active')?'success':'danger'" size="small">
                {{ (row.status===1||row.status==='active')?'正常':'禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="注册时间" width="175" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-switch
                v-if="row.role !== 1 && row.role !== 'admin'"
                :model-value="row.status === 1 || row.status === 'active'"
                :loading="row._toggling"
                @change="toggleStatus(row)"
                active-text="正常" inactive-text="禁用"
              />
            </template>
          </el-table-column>
        </el-table>
        <div v-if="total > 0" style="text-align:center;margin-top:20px">
          <el-pagination
            v-model:current-page="page" :page-size="size" :total="total"
            layout="total, prev, pager, next" @current-change="loadUsers" background small
          />
        </div>
      </template>
    </el-skeleton>
  </el-card>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsers, toggleUserStatus } from '../../api/admin'

const users = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const loading = ref(false)

onMounted(() => loadUsers())

async function loadUsers() {
  loading.value = true
  try {
    const res = await listUsers(page.value, size.value, keyword.value)
    if (res.code === 200) { users.value = res.data.records || []; total.value = res.data.total || 0 }
  } catch(e) { users.value = []; total.value = 0 }
  loading.value = false
}

function search() { page.value = 1; loadUsers() }

async function toggleStatus(row) {
  const newStatus = (row.status === 1 || row.status === 'active') ? 0 : 1
  const label = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确认${label}用户「${row.username}」？`, '提示')
    row._toggling = true
    await toggleUserStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(`已${label}`)
  } catch(e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
  row._toggling = false
}
</script>
