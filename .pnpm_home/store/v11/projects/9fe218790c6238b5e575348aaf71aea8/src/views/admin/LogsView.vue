<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:8px">
        <span><el-icon><Document /></el-icon> 操作日志</span>
        <div style="display:flex;gap:8px;align-items:center">
          <el-radio-group v-model="logType" @change="loadLogs" size="small">
            <el-radio-button :value="0">全部</el-radio-button>
            <el-radio-button :value="1">用户操作</el-radio-button>
            <el-radio-button :value="2">AI调用</el-radio-button>
          </el-radio-group>
          <el-button size="small" @click="loadLogs" :loading="loading" icon="Refresh">刷新</el-button>
        </div>
      </div>
    </template>

    <!-- 加载骨架 -->
    <el-skeleton :loading="loading && !logs.length" animated>
      <template #template>
        <div v-for="i in 5" :key="i" style="display:flex;gap:12px;padding:12px 0;border-bottom:1px solid #eee">
          <el-skeleton-item variant="text" style="width:60px" />
          <el-skeleton-item variant="text" style="width:80px" />
          <el-skeleton-item variant="text" style="width:100px" />
          <el-skeleton-item variant="text" style="flex:1" />
          <el-skeleton-item variant="text" style="width:130px" />
        </div>
      </template>

      <template #default>
        <!-- 空状态 -->
        <el-empty v-if="!logs.length && !loading" description="暂无操作日志" :image-size="100">
          <p style="color:#909399;font-size:13px;margin-bottom:12px">用户操作和 AI 调用记录将显示在此处</p>
          <el-button size="small" @click="loadLogs">刷新</el-button>
        </el-empty>

        <!-- 数据表格 -->
        <el-table v-else :data="logs" stripe style="width:100%" v-loading="loading" element-loading-text="加载中...">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="userId" label="用户ID" width="80" />
          <el-table-column label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.logType===2?'warning':'info'" size="small" effect="plain">
                {{ row.logType===2?'AI调用':'用户操作' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="operation" label="操作" min-width="160" show-overflow-tooltip />
          <el-table-column label="结果" width="220">
            <template #default="{ row }">
              <span style="color:#909399;font-size:12px">{{ row.result }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="costTime" label="耗时(ms)" width="90" align="center" />
          <el-table-column prop="ip" label="IP" width="130" />
          <el-table-column prop="createdAt" label="时间" width="175" />
        </el-table>

        <!-- 分页 -->
        <div v-if="total > 0" style="text-align:center;margin-top:20px">
          <el-pagination
            v-model:current-page="page"
            :page-size="size"
            :total="total"
            layout="total, prev, pager, next, jumper"
            @current-change="loadLogs"
            background
            small
          />
        </div>
      </template>
    </el-skeleton>
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
const loading = ref(false)

onMounted(() => loadLogs())

async function loadLogs() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (logType.value) params.logType = logType.value
    const res = await request.get('/admin/logs', { params })
    if (res.code === 200) {
      logs.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch(e) {
    // 后端未运行时使用空数据友好降级
    logs.value = []
    total.value = 0
  }
  loading.value = false
}
</script>
