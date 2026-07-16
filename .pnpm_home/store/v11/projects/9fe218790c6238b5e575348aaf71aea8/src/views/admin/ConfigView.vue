<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span><el-icon><Tools /></el-icon> 系统配置</span>
        <el-button type="primary" @click="saveConfig" :loading="saving">保存配置</el-button>
      </div>
    </template>
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div v-for="i in 6" :key="i" style="display:flex;gap:12px;padding:14px 0">
          <el-skeleton-item variant="text" style="width:180px" />
          <el-skeleton-item variant="text" style="flex:1" />
        </div>
      </template>
      <template #default>
        <el-form :model="configMap" label-width="180px" v-if="Object.keys(configMap).length">
          <el-form-item label="大模型API密钥">
            <el-input v-model="configMap['ai_model_api_key']" placeholder="Coze扣子API Key" type="password" show-password />
          </el-form-item>
          <el-form-item label="大模型API地址">
            <el-input v-model="configMap['ai_model_api_url']" placeholder="https://api.coze.cn/open_api/v2/chat" />
          </el-form-item>
          <el-form-item label="每日生成次数限制">
            <el-input-number v-model="numLimit" :min="1" :max="10000" @update:model-value="v => configMap['ai_daily_limit'] = String(v)" />
          </el-form-item>
          <el-form-item label="图片生成API密钥">
            <el-input v-model="configMap['image_gen_api_key']" type="password" show-password />
          </el-form-item>
          <el-form-item label="图片生成API地址">
            <el-input v-model="configMap['image_gen_api_url']" />
          </el-form-item>
          <el-form-item label="天气API密钥">
            <el-input v-model="configMap['weather_api_key']" type="password" show-password />
          </el-form-item>
          <el-divider />
          <el-form-item label="系统名称">
            <el-input v-model="configMap['system_name']" />
          </el-form-item>
        </el-form>
        <el-empty v-else description="暂无配置数据" :image-size="80" />
      </template>
    </el-skeleton>
  </el-card>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfig, updateConfig } from '../../api/admin'

const configMap = reactive({})
const saving = ref(false)
const loading = ref(true)

const numLimit = computed({
  get: () => parseInt(configMap['ai_daily_limit']) || 100,
  set: v => { configMap['ai_daily_limit'] = String(v) },
})

onMounted(async () => {
  try {
    const res = await getConfig()
    if (res.code === 200 && res.data) {
      res.data.forEach(item => { configMap[item.configKey] = item.configValue })
      // 存下 id 供保存使用
      res.data.forEach(item => { configMap['_id_' + item.configKey] = item.id })
    }
  } catch(e) {}
  loading.value = false
})

async function saveConfig() {
  saving.value = true
  try {
    // 后端 updateConfig 需要 id + configValue
    const items = Object.entries(configMap)
      .filter(([k]) => !k.startsWith('_'))
      .map(([key, value]) => ({
        id: configMap['_id_' + key] || null,
        configKey: key,
        configValue: String(value ?? ''),
      }))
    await updateConfig(items)
    ElMessage.success('配置已保存')
  } catch(e) { ElMessage.error('保存失败') }
  saving.value = false
}
</script>
