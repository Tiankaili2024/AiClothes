<template>
  <div class="history-container">
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span class="card-title"><el-icon><Clock /></el-icon> 穿搭记录</span>
          <div>
            <el-button type="danger" text @click="handleClear" v-if="records.length">清空记录</el-button>
          </div>
        </div>
      </template>
      <div v-if="records.length">
        <el-timeline>
          <el-timeline-item
            v-for="item in records"
            :key="item.id"
            :timestamp="item.createdAt"
            placement="top"
          >
            <el-card shadow="hover" style="margin-bottom:8px">
              <el-row :gutter="16">
                <el-col :span="6">
                  <el-image :src="item.imageUrl" fit="cover" style="width:100%;height:150px;border-radius:4px" />
                </el-col>
                <el-col :span="18">
                  <div style="margin-bottom:8px">
                    <el-tag size="small" v-if="item.styleTags">{{ item.styleTags }}</el-tag>
                    <el-tag size="small" type="success" v-if="item.status === 1">生成成功</el-tag>
                    <el-tag size="small" type="danger" v-else-if="item.status === 2">失败</el-tag>
                  </div>
                  <p style="color:#606266;margin-bottom:6px"><strong>需求：</strong>{{ item.userInput }}</p>
                  <p style="color:#909399;font-size:12px;margin-bottom:8px">{{ item.createdAt }}</p>
                  <div style="display:flex;gap:8px">
                    <el-button size="small" text type="primary" @click="viewDetail(item)">查看</el-button>
                    <el-button size="small" text type="warning" @click="reuseInput(item)">复用需求</el-button>
                    <el-button size="small" text type="danger" @click="handleDelete(item.id)">删除</el-button>
                  </div>
                </el-col>
              </el-row>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <div style="text-align:center;margin-top:20px">
          <el-pagination
            v-model:current-page="page"
            :page-size="size"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadRecords"
          />
        </div>
      </div>
      <el-empty v-else description="暂无穿搭记录" />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="穿搭详情" width="700px">
      <el-row v-if="detail" :gutter="20">
        <el-col :span="12">
          <el-image :src="detail.imageUrl" fit="contain" style="width:100%;border-radius:8px" />
        </el-col>
        <el-col :span="12">
          <p><strong>需求：</strong>{{ detail.userInput }}</p>
          <p><strong>Prompt：</strong>{{ detail.prompt }}</p>
          <p><strong>风格：</strong>{{ detail.styleTags }}</p>
          <p><strong>时间：</strong>{{ detail.createdAt }}</p>
          <div style="margin-top:12px">
            <el-button type="primary" @click="openExternal(detail.imageUrl)">查看原图</el-button>
          </div>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRecords, deleteRecord, clearRecords } from '../../api/outfit'
import { useRouter } from 'vue-router'

const router = useRouter()
const records = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const detail = ref(null)

onMounted(() => loadRecords())

async function loadRecords() {
  try {
    const res = await getRecords(page.value, size.value)
    if (res.code === 200) {
      records.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {}
}

function viewDetail(item) {
  detail.value = item
  detailVisible.value = true
}

function reuseInput(item) {
  router.push({ path: '/outfit', query: { input: item.userInput } })
}

async function handleDelete(id) {
  try {
    await deleteRecord(id)
    ElMessage.success('已删除')
    loadRecords()
  } catch (e) {}
}

function handleClear() {
  ElMessageBox.confirm('确认清空所有穿搭记录？', '提示').then(async () => {
    try {
      await clearRecords()
      ElMessage.success('已清空')
      loadRecords()
    } catch (e) {}
  }).catch(() => {})
}

function openExternal(url) {
  window.open(url, '_blank')
}
</script>

<style scoped>
.history-container { max-width: 900px; margin: 0 auto; }
.card-title { font-size: 18px; font-weight: bold; display: flex; align-items: center; gap: 6px; }
</style>
