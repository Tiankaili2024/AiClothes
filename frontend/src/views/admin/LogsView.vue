<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span><el-icon><Document /></el-icon> 操作日志</span>
        <div>
          <el-radio-group v-model="logType" @change="loadLogs" size="small">
            <el-radio-button :value="0">全部</el-radio-button>
            <el-radio-button :value="1">用户操作</el-radio-button>
            <el-radio-button :value="2">AI调用</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </template>
    <el-table :data="logs" stripe style="width:100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }"><el-tag :type="row.logType===2?'warning':'info'" size="small">{{ row.logType===2?'AI调用':'用户操作' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="operation" label="操作" min-width="150" />
      <el-table-column label="结果" width="200"><template #default="{ row }"><span style="color:#909399;font-size:12px">{{ row.result }}</span></template></el-table-column>
      <el-table-column prop="costTime" label="耗时(ms)" width="90" />
      <el-table-column prop="ip" label="IP" width="130" />
      <el-table-column prop="createdAt" label="时间" width="180" />
    </el-table>
    <div style="text-align:center;margin-top:20px">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="prev, pager, next" @current-change="loadLogs" />
    </div>
  </el-card>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'
const logs = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const logType = ref(0)
onMounted(() => loadLogs())
async function loadLogs() {
  try {
    const params = { page: page.value, size: size.value }
    if (logType.value) params.logType = logType.value
    const res = await request.get('/admin/logs', { params })
    if (res.code === 200) { logs.value = res.data.records || []; total.value = res.data.total || 0 }
  } catch(e) {}
}
</script>