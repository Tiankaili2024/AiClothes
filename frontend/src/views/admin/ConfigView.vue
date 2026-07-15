<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span><el-icon><Tools /></el-icon> 系统配置</span>
        <el-button type="primary" @click="saveConfig" :loading="saving">保存配置</el-button>
      </div>
    </template>
    <el-form :model="configMap" label-width="180px">
      <el-form-item label="大模型API密钥">
        <el-input v-model="configMap['ai_model_api_key']" placeholder="Coze扣子API Key" type="password" show-password />
      </el-form-item>
      <el-form-item label="大模型API地址">
        <el-input v-model="configMap['ai_model_api_url']" placeholder="https://api.coze.cn/open_api/v2/chat" />
      </el-form-item>
      <el-form-item label="每日生成次数限制">
        <el-input-number v-model="configMap['ai_daily_limit']" :min="1" :max="10000" />
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
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfig, updateConfig } from '../../api/admin'

const configMap = reactive({})
const saving = ref(false)

onMounted(async () => {
  try {
    const res = await getConfig()
    if (res.code === 200 && res.data) {
      res.data.forEach(item => { configMap[item.configKey] = item.configValue })
    }
  } catch (e) {}
})

async function saveConfig() {
  saving.value = true
  try {
    const items = Object.entries(configMap).map(([key, value]) => ({
      configKey: key,
      configValue: String(value),
    }))
    await updateConfig(items)
    ElMessage.success('配置已保存')
  } catch (e) {}
  saving.value = false
}
</script>
