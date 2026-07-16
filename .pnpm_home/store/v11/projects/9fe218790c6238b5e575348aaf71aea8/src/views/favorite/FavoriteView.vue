<template>
  <div class="favorite-container">
    <el-card shadow="never">
      <template #header>
        <span class="card-title"><el-icon><Star /></el-icon> 我的收藏</span>
      </template>
      <div v-if="records.length">
        <el-row :gutter="16">
          <el-col :span="8" v-for="item in records" :key="item.id" style="margin-bottom:16px">
            <el-card shadow="hover" :body-style="{ padding: '12px' }">
              <el-image :src="item.imageUrl" fit="cover" style="width:100%;height:240px;border-radius:4px;cursor:pointer"
                @click="openExternal(item.imageUrl)" />
              <div style="padding:8px 0">
                <el-tag size="small" v-if="item.styleTags">{{ item.styleTags }}</el-tag>
                <p style="margin:8px 0;font-size:13px;color:#606266">{{ item.userInput?.substring(0,30) }}...</p>
                <div style="display:flex;gap:8px">
                  <el-button size="small" text type="danger" @click="delFavorite(item.id)">
                    <el-icon><Delete /></el-icon> 取消收藏
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <div style="text-align:center;margin-top:20px">
          <el-pagination
            v-model:current-page="page"
            :page-size="size"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadFavorites"
          />
        </div>
      </div>
      <el-empty v-else description="暂无收藏" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFavorites, removeFavorite } from '../../api/favorite'

const records = ref([])
const page = ref(1)
const size = ref(12)
const total = ref(0)

onMounted(() => loadFavorites())

async function loadFavorites() {
  try {
    const res = await getFavorites(page.value, size.value)
    if (res.code === 200) {
      records.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {}
}

async function delFavorite(recordId) {
  try {
    await removeFavorite(recordId)
    ElMessage.success('已取消收藏')
    loadFavorites()
  } catch (e) {}
}

function openExternal(url) {
  window.open(url, '_blank')
}
</script>

<style scoped>
.favorite-container { max-width: 1100px; margin: 0 auto; }
.card-title { font-size: 18px; font-weight: bold; display: flex; align-items: center; gap: 6px; }
</style>
