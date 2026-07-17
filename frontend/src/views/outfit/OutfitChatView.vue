<template>
  <div class="outfit-container">
    <el-row :gutter="20">
      <!-- 左侧：对话区 -->
      <el-col :span="14">
        <el-card shadow="never" class="chat-card">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="card-title"><el-icon><ChatDotRound /></el-icon> AI穿搭对话</span>
              <el-tag type="success" v-if="weatherInfo">今日 {{ weatherInfo }}</el-tag>
            </div>
          </template>
          <div class="chat-messages" ref="chatRef">
            <div v-for="(msg, i) in messages" :key="i" :class="'msg-' + msg.role">
              <div class="msg-avatar">
                <el-avatar :size="36" v-if="msg.role === 'ai'" icon="MagicStick" style="background:#409eff" />
                <el-avatar :size="36" v-else icon="UserFilled" style="background:#67c23a" />
              </div>
              <div class="msg-content">
                <div class="msg-bubble">{{ msg.content }}</div>
                <div v-if="msg.imageUrl" class="msg-image">
                  <el-image :src="msg.imageUrl" fit="contain" style="width:100%;max-height:400px;border-radius:8px;cursor:pointer"
                    :preview-src-list="[msg.imageUrl]"
                    preview-teleported>
                    <template #error>
                      <div style="padding:20px;text-align:center;color:#909399">图片加载中...</div>
                    </template>
                  </el-image>
                </div>
                <div v-if="msg.recordId" class="msg-actions">
                  <el-button size="small" text type="primary" @click="addFav(msg.recordId)">
                    <el-icon><Star /></el-icon> {{ msg.fav ? '已收藏' : '收藏' }}
                  </el-button>
                  <el-button size="small" text type="primary" :preview-src-list="[msg.imageUrl]"
                    preview-teleported>
                    <el-icon><Download /></el-icon> 下载
                  </el-button>
                  <el-button size="small" text type="warning" @click="regenerate(msg)">
                    <el-icon><Refresh /></el-icon> 重新生成
                  </el-button>
                </div>
              </div>
            </div>
            <div v-if="generating" class="msg-ai">
              <div class="msg-avatar">
                <el-avatar :size="36" icon="MagicStick" style="background:#409eff" />
              </div>
              <div class="msg-content">
                <div class="msg-bubble"><el-icon class="is-loading"><Loading /></el-icon> AI正在思考穿搭方案...</div>
              </div>
            </div>
          </div>
        </el-card>
        <!-- 快捷模板 -->
        <el-card shadow="never" style="margin-top:16px">
          <div style="display:flex;gap:8px;flex-wrap:wrap">
            <el-button v-for="tpl in templates" :key="tpl.label" @click="useTemplate(tpl)">
              <el-icon><component :is="tpl.icon" /></el-icon> {{ tpl.label }}
            </el-button>
          </div>
        </el-card>
        <!-- 输入区 -->
        <el-card shadow="never" style="margin-top:16px">
          <div style="display:flex;gap:12px;align-items:center;margin-bottom:12px">
            <span style="white-space:nowrap;font-size:14px;color:#606266">推荐城市：</span>
            <el-input
              v-model="cityInput"
              placeholder="输入城市（如：北京），留空则使用档案中的常驻城市"
              clearable
              style="flex:1"
              @change="onCityChange"
            >
              <template #prepend>城市</template>
            </el-input>
            <el-button size="small" type="success" @click="loadWeather" :loading="weatherLoading">
              <el-icon><Refresh /></el-icon> 刷新天气
            </el-button>
          </div>
          <el-input
            v-model="userInput"
            type="textarea"
            :rows="3"
            placeholder="输入你的穿搭需求，例如：今天25度晴天，黄皮小个子，上班通勤，温柔浅色穿搭"
          />
          <div style="margin-top:12px;display:flex;justify-content:space-between;align-items:center">
            <span style="color:#909399;font-size:12px">今日已生成 {{ todayCount }} 次</span>
            <el-button type="primary" size="large" @click="sendMessage" :loading="generating" icon="Promotion">生成穿搭</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：穿搭效果 + 方案解析 -->
      <el-col :span="10">
        <el-card shadow="never" class="result-card">
          <template #header>
            <div>
              <span class="card-title"><el-icon><Picture /></el-icon> 穿搭效果</span>
              <div v-if="currentPlan.image_url" style="margin-top:4px">
                <el-link type="primary" :underline="false" style="font-size:11px;cursor:pointer" @click="previewImage(currentPlan.image_url)">
                  <el-icon><Link /></el-icon> 查看完整图片
                </el-link>
              </div>
            </div>
          </template>

          <!-- 无数据时 -->
          <div v-if="!schemes.length" class="outfit-empty">
            <div class="empty-hint">
              <el-icon :size="48"><MagicStick /></el-icon>
              <p>输入需求生成穿搭方案</p>
              <p class="empty-sub">效果与解析将展示在此处</p>
            </div>
          </div>

          <!-- 有数据时 -->
          <div v-else class="outfit-result">
            <!-- 季节搭配主标题 -->
            <div class="season-title">季节搭配</div>

            <!-- 当前方案内容 -->
            <div class="scheme-content">
              <!-- 穿搭图片 -->
              <div class="outfit-image-wrap">
                <el-image
                  v-if="currentPlan.prompt"
                  :src="currentPlan.image_url || '/api/placeholder/outfit?seed=' + Date.now()"
                  fit="contain"
                  class="outfit-image"
                  :preview-src-list="currentPlan.image_url ? [currentPlan.image_url] : []"
                  preview-teleported
                >
                  <template #error>
                    <div class="outfit-image-placeholder">
                      <el-icon :size="36"><Picture /></el-icon>
                      <span>图片加载失败，点击重试</span>
                    </div>
                  </template>
                </el-image>
                <div v-else class="outfit-image-placeholder">
                  <el-icon :size="36"><Picture /></el-icon>
                  <span>穿搭效果图</span>
                </div>
                <div v-if="currentPlan.image_url" style="margin-top:6px;text-align:center">
                  <el-link type="primary" :underline="false" style="font-size:12px;cursor:pointer" @click="previewImage(currentPlan.image_url)">
                    查看原图
                  </el-link>
                </div>
              </div>

              <!-- 方案解析 -->
              <div class="plan-detail">
                <el-descriptions :column="1" border size="small" class="plan-desc">
                  <el-descriptions-item label="风格名称">{{ currentPlan.scheme_name || '—' }}</el-descriptions-item>
                  <el-descriptions-item label="风格">{{ currentPlan.style || '—' }}</el-descriptions-item>
                  <el-descriptions-item label="配色">{{ currentPlan.color || '—' }}</el-descriptions-item>
                  <el-descriptions-item label="上装">{{ currentPlan.top || '—' }}</el-descriptions-item>
                  <el-descriptions-item label="下装">{{ currentPlan.bottom || '—' }}</el-descriptions-item>
                  <el-descriptions-item label="鞋履">{{ currentPlan.shoes || '—' }}</el-descriptions-item>
                  <el-descriptions-item label="配饰">{{ currentPlan.accessory || '—' }}</el-descriptions-item>
                </el-descriptions>

                <!-- 搭配理由 -->
                <div class="match-reason" v-if="currentPlan.match_reason">
                  <h4>搭配理由</h4>
                  <p>{{ currentPlan.match_reason }}</p>
                </div>

                <!-- 评分 -->
                <div class="score-section" v-if="currentPlan.score_total">
                  <h4>综合评分</h4>
                  <div class="score-bar">
                    <div class="score-label">整体</div>
                    <el-progress :percentage="currentPlan.score_total || 0" :stroke-width="12" color="#8AB4D0" />
                  </div>
                  <div class="score-bar">
                    <div class="score-label">身材</div>
                    <el-progress :percentage="currentPlan.score_body || 0" :stroke-width="10" color="#A3C8E0" />
                  </div>
                  <div class="score-bar">
                    <div class="score-label">天气</div>
                    <el-progress :percentage="currentPlan.score_weather || 0" :stroke-width="10" color="#B8D6E8" />
                  </div>
                  <div class="score-bar">
                    <div class="score-label">场合</div>
                    <el-progress :percentage="currentPlan.score_occasion || 0" :stroke-width="10" color="#C8D8E0" />
                  </div>
                  <div class="score-bar">
                    <div class="score-label">配色</div>
                    <el-progress :percentage="currentPlan.score_color || 0" :stroke-width="10" color="#D4C8B0" />
                  </div>
                </div>

                <!-- 优化建议 -->
                <div class="optimize-advice" v-if="currentPlan.optimize_advice">
                  <h4>优化建议</h4>
                  <el-alert :title="currentPlan.optimize_advice" type="info" :closable="false" show-icon />
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from "vue"
import { ElMessage } from 'element-plus'
import { generateOutfit, getTodayCount } from '../../api/outfit'
import { getWeather } from '../../api/weather'
import { addFavorite, checkFavorite } from '../../api/favorite'
import { getUser } from '../../utils/auth'

const userInput = ref('')
const generating = ref(false)
const messages = ref([])
const schemes = ref([])
const activeScheme = ref(0)
const chatRef = ref(null)
const todayCount = ref(0)
const weatherInfo = ref('')
const user = ref(getUser())
const cityInput = ref('')
const weatherLoading = ref(false)

const DEFAULT_CITY = '北京'

const currentPlan = computed(() => schemes.value[activeScheme.value] || {})

const templates = [
  { label: '通勤穿搭', icon: 'Briefcase', text: '上班通勤穿搭，得体大方' },
  { label: '约会穿搭', icon: 'Cherry', text: '约会穿搭，温柔显气质' },
  { label: '出游穿搭', icon: 'Sunny', text: '周末出游穿搭，舒适好看' },
  { label: '运动穿搭', icon: 'TrendCharts', text: '运动健身穿搭，活力休闲' },
  { label: '居家穿搭', icon: 'HomeFilled', text: '居家休闲穿搭，舒适为先' },
  { label: '礼服穿搭', icon: 'Trophy', text: '正式场礼服穿搭，优雅高级' },
]

onMounted(async () => {
  cityInput.value = user.value?.city || DEFAULT_CITY
  await loadWeather()
  await loadTodayCount()
})

async function loadWeather() {
  weatherLoading.value = true
  try {
    const city = cityInput.value || DEFAULT_CITY
    const res = await getWeather(city)
    if (res.code === 200 && res.data) {
      weatherInfo.value = res.data.city + ' ' + res.data.weather + ' ' + res.data.temperature + '°C'
    }
  } catch (e) {
    weatherInfo.value = cityInput.value + ' 晴 25°C'
  }
  weatherLoading.value = false
}

function onCityChange() {
  if (cityInput.value) loadWeather()
}

async function loadTodayCount() {
  try {
    const res = await getTodayCount()
    if (res.code === 200) todayCount.value = res.data
  } catch (e) {}
}

function useTemplate(tpl) {
  userInput.value = (weatherInfo.value ? weatherInfo.value + '，' : '') + tpl.text
}

async function sendMessage() {
  if (!userInput.value.trim()) {
    ElMessage.warning('请输入穿搭需求')
    return
  }
  generating.value = true
  const inputText = userInput.value
  messages.value.push({ role: 'user', content: inputText })
  userInput.value = ''
  await nextTick()
  scrollToBottom()

  let lastError = ''
  for (let attempt = 1; attempt <= 2; attempt++) {
    try {
      const res = await generateOutfit({ userInput: inputText, city: cityInput.value || DEFAULT_CITY })
      if (res.code === 200 && res.data) {
        const record = res.data
        let plans = []
        try {
          const parsed = JSON.parse(record.parsedParams || '[]')
          plans = Array.isArray(parsed) ? parsed : (parsed ? [parsed] : [])
        } catch (e) { plans = [] }
        if (!plans.length) {
          plans = [{ scheme_name: '推荐搭配', style: '通勤', color: '浅色系', top: '—', bottom: '—', shoes: '—', accessory: '—' }]
        }
        if (record.imageUrl && plans.length > 0) plans[0].image_url = record.imageUrl
        schemes.value = plans
        activeScheme.value = 0

        const planNames = plans.map((p, i) => '方案' + (i + 1) + ': ' + (p.scheme_name || p.style || '')).join('、')
        messages.value.push({
          role: 'ai',
          content: (record.imageUrl && !record.imageUrl.includes('placeholder')) ? '图片已生成' : '图片错误，请重新上传图片',
          imageUrl: record.imageUrl || '',
          recordId: record.id,
          params: plans,
        })
        await checkFavStatus(record.id).catch(() => {})
        await loadTodayCount()
        generating.value = false
        return
      } else {
        lastError = res.msg || '未知错误'
      }
    } catch (e) {
      lastError = e.message || '网络错误'
      console.error('Generate attempt ' + attempt + ' failed:', lastError)
    }
    if (attempt < 2) await new Promise(r => setTimeout(r, 3000))
  }
  generating.value = false
  messages.value.push({ role: 'ai', content: '生成失败：' + lastError + '，请稍后重试' })
  await nextTick()
  scrollToBottom()
}

async function checkFavStatus(recordId) {
  try {
    const res = await checkFavorite(recordId)
    if (res.code === 200 && res.data) {
      const last = messages.value[messages.value.length - 1]
      if (last) last.fav = true
    }
  } catch (e) {}
}

async function addFav(recordId) {
  try { await addFavorite(recordId); ElMessage.success('已收藏') } catch (e) {}
}

function regenerate(msg) {
  if (msg.params && msg.params.length) {
    userInput.value = '重新生成类似的穿搭，风格保持' + (msg.params[0]?.style || '')
  } else {
    userInput.value = '重新生成一套穿搭'
  }
}

function previewImage(url) {
  if (url) window.open(url, '_blank')
}

function scrollToBottom() {
  if (chatRef.value) {
    chatRef.value.scrollTop = chatRef.value.scrollHeight
  }
}
</script>

<style scoped>
.outfit-container { max-width: 1400px; margin: 0 auto; padding: 0 4px; }
.chat-card { height: 500px; display: flex; flex-direction: column; }
.chat-messages { flex: 1; overflow-y: auto; padding: 10px 0; }
.msg-user, .msg-ai { display: flex; margin-bottom: 16px; gap: 10px; }
.msg-user { flex-direction: row-reverse; }
.msg-avatar { flex-shrink: 0; }
.msg-content { max-width: 80%; }
.msg-bubble { padding: 12px 16px; border-radius: 12px; line-height: 1.6; font-size: 14px; white-space: pre-line; }
.msg-user .msg-bubble { background: #409eff; color: #fff; border-bottom-right-radius: 4px; }
.msg-ai .msg-bubble { background: #f0f2f5; color: #303133; border-bottom-left-radius: 4px; }
.msg-image { margin-top: 8px; }
.msg-actions { margin-top: 8px; display: flex; gap: 4px; flex-wrap: wrap; }
.card-title { font-size: 16px; font-weight: bold; display: flex; align-items: center; gap: 6px; }

.result-card { min-height: 580px; }

.outfit-empty { min-height: 480px; display: flex; align-items: center; justify-content: center; }
.empty-hint { text-align: center; color: #b0c2d0; }
.empty-hint p { margin: 12px 0 4px; font-size: 15px; color: #8a9eae; }
.empty-sub { font-size: 13px !important; color: #b8ccda !important; }

.season-title { font-size: 18px; font-weight: bold; color: #3d4f5e; margin-bottom: 12px; padding-bottom: 8px; border-bottom: 2px solid #8AB4D0; text-align: center; }
.outfit-image-wrap { margin-bottom: 16px; border-radius: 12px; overflow: hidden; background: #f5f8fa; }
.outfit-image { width: 100%; min-height: 280px; display: block; }
.outfit-image-placeholder { min-height: 240px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; color: #b8ccda; font-size: 14px; }

.plan-detail { text-align: left; }
.plan-desc { margin-bottom: 16px; }
:deep(.plan-desc .el-descriptions__label) { width: 70px; color: #6a7e8e; }
:deep(.plan-desc .el-descriptions__content) { color: #3d4f5e; }

.match-reason { margin-bottom: 16px; }
.match-reason h4 { font-size: 14px; color: #3d4f5e; margin: 0 0 6px; }
.match-reason p { font-size: 13px; color: #6a7e8e; line-height: 1.7; margin: 0; padding: 10px 14px; background: #f8fafc; border-radius: 8px; }

.score-section { margin-bottom: 16px; }
.score-section h4 { font-size: 14px; color: #3d4f5e; margin: 0 0 10px; }
.score-bar { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.score-label { width: 40px; font-size: 12px; color: #8a9eae; text-align: right; flex-shrink: 0; }

.optimize-advice { margin-bottom: 4px; }
.optimize-advice h4 { font-size: 14px; color: #3d4f5e; margin: 0 0 8px; }
</style>
