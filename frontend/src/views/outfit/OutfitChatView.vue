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
                  <el-image :src="msg.imageUrl" fit="contain" style="width:100%;max-height:400px;border-radius:8px"
                    @click="previewImage(msg.imageUrl)" />
                </div>
                <div v-if="msg.recordId" class="msg-actions">
                  <el-button size="small" text type="primary" @click="addFav(msg.recordId)">
                    <el-icon><Star /></el-icon> {{ msg.fav ? '已收藏' : '收藏' }}
                  </el-button>
                  <el-button size="small" text type="primary" @click="downloadImage(msg.imageUrl)">
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
      <!-- 右侧：穿搭展示 -->
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><span class="card-title"><el-icon><Picture /></el-icon> 穿搭效果</span></template>
          <div v-if="currentImage" class="outfit-display">
            <el-image :src="currentImage" fit="contain" style="width:100%;min-height:400px;border-radius:8px" />
            <div class="outfit-params" v-if="currentParams">
              <el-divider />
              <h4>穿搭方案解析</h4>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="风格">{{ currentParams.style }}</el-descriptions-item>
                <el-descriptions-item label="色系">{{ currentParams.color }}</el-descriptions-item>
                <el-descriptions-item label="上装">{{ currentParams.top }}</el-descriptions-item>
                <el-descriptions-item label="下装">{{ currentParams.bottom }}</el-descriptions-item>
                <el-descriptions-item label="鞋子">{{ currentParams.shoes }}</el-descriptions-item>
                <el-descriptions-item label="配饰">{{ currentParams.accessory }}</el-descriptions-item>
                <el-descriptions-item label="场合">{{ currentParams.occasion }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
          <div v-else class="outfit-empty">
            <el-empty description="输入需求，开始生成穿搭吧~" :image-size="120" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { generateOutfit, getTodayCount } from '../../api/outfit'
import { addFavorite, checkFavorite } from '../../api/favorite'
import { getWeather } from '../../api/weather'
import { getUser } from '../../utils/auth'

const userInput = ref('')
const messages = ref([])
const generating = ref(false)
const currentImage = ref('')
const currentParams = ref(null)
const chatRef = ref(null)
const todayCount = ref(0)
const weatherInfo = ref('')
const user = ref(getUser())
const cityInput = ref('')
const weatherLoading = ref(false)

const DEFAULT_CITY = '北京'

const templates = [
  { label: '通勤穿搭', icon: 'Briefcase', text: '上班通勤穿搭，得体大方' },
  { label: '约会穿搭', icon: 'Cherry', text: '约会穿搭，温柔显气质' },
  { label: '出游穿搭', icon: 'Sunny', text: '周末出游穿搭，舒适好看' },
  { label: '运动穿搭', icon: 'TrendCharts', text: '运动健身穿搭，活力休闲' },
  { label: '居家穿搭', icon: 'HomeFilled', text: '居家休闲穿搭，舒适为主' },
  { label: '礼服穿搭', icon: 'Trophy', text: '正式场合礼服穿搭，优雅高级' },
]

onMounted(async () => {
  // 初始化城市：优先档案城市，否则默认北京
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
  if (cityInput.value) {
    loadWeather()
  }
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

  try {
    // 传入城市参数
    const res = await generateOutfit({
      userInput: inputText,
      city: cityInput.value || DEFAULT_CITY
    })
    if (res.code === 200 && res.data) {
      const record = res.data
      let params = null
      try {
        params = JSON.parse(record.parsedParams || '{}')
      } catch (e) {}
      currentParams.value = params
      currentImage.value = record.imageUrl
      messages.value.push({
        role: 'ai',
        content: '穿搭方案已生成！点击右侧查看详细搭配 🎉',
        imageUrl: record.imageUrl,
        recordId: record.id,
        params: params,
      })
      await checkFavStatus(record.id)
      await loadTodayCount()
    }
  } catch (e) {
    messages.value.push({ role: 'ai', content: '生成失败，请稍后重试' })
  }
  generating.value = false
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
  try {
    await addFavorite(recordId)
    ElMessage.success('已收藏')
  } catch (e) {}
}

function downloadImage(url) {
  if (url) window.open(url, '_blank')
}

function regenerate(msg) {
  if (msg.params) {
    userInput.value = '重新生成类似的穿搭，风格保持' + (msg.params.style || '')
  } else {
    userInput.value = '重新生成一套穿搭'
  }
}

function previewImage(url) {
  window.open(url, '_blank')
}

function scrollToBottom() {
  if (chatRef.value) {
    chatRef.value.scrollTop = chatRef.value.scrollHeight
  }
}

function openExternal(url) {
  window.open(url, '_blank')
}
</script>

<style scoped>
.outfit-container { max-width: 1400px; margin: 0 auto; }
.chat-card { height: 500px; display: flex; flex-direction: column; }
.chat-messages { flex: 1; overflow-y: auto; padding: 10px 0; }
.msg-user, .msg-ai { display: flex; margin-bottom: 16px; gap: 10px; }
.msg-user { flex-direction: row-reverse; }
.msg-avatar { flex-shrink: 0; }
.msg-content { max-width: 80%; }
.msg-bubble { padding: 12px 16px; border-radius: 12px; line-height: 1.6; font-size: 14px; }
.msg-user .msg-bubble { background: #409eff; color: #fff; border-bottom-right-radius: 4px; }
.msg-ai .msg-bubble { background: #f0f2f5; color: #303133; border-bottom-left-radius: 4px; }
.msg-image { margin-top: 8px; }
.msg-actions { margin-top: 8px; display: flex; gap: 4px; }
.card-title { font-size: 16px; font-weight: bold; display: flex; align-items: center; gap: 6px; }
.outfit-display { text-align: center; }
.outfit-empty { min-height: 400px; display: flex; align-items: center; justify-content: center; }
</style>
