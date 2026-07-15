<template>
  <div class="profile-container">
    <el-card shadow="never">
      <template #header><span class="card-title">个人穿搭档案</span></template>
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio value="男">男</el-radio>
                <el-radio value="女">女</el-radio>
                <el-radio value="">未设置</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身高(cm)"><el-input-number v-model="form.height" :min="100" :max="220" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)"><el-input-number v-model="form.weight" :min="30" :max="200" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身材类型">
              <el-select v-model="form.bodyType" placeholder="选择身材" style="width:100%">
                <el-option label="小个子" value="小个子" /><el-option label="标准身材" value="标准身材" />
                <el-option label="梨形" value="梨形" /><el-option label="苹果型" value="苹果型" />
                <el-option label="微胖" value="微胖" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="肤色">
              <el-select v-model="form.skinTone" placeholder="选择肤色" style="width:100%">
                <el-option label="冷白皮" value="冷白皮" /><el-option label="黄一白" value="黄一白" />
                <el-option label="黄黑皮" value="黄黑皮" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="风格偏好">
          <el-checkbox-group v-model="stylePrefs">
            <el-checkbox value="通勤" label="通勤" /><el-checkbox value="温柔" label="温柔" />
            <el-checkbox value="甜酷" label="甜酷" /><el-checkbox value="国风" label="国风" />
            <el-checkbox value="运动" label="运动" /><el-checkbox value="休闲" label="休闲" />
            <el-checkbox value="复古" label="复古" /><el-checkbox value="礼服" label="礼服" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="色系偏好">
          <el-checkbox-group v-model="colorPrefs">
            <el-checkbox value="浅色系" label="浅色系" /><el-checkbox value="深色系" label="深色系" />
            <el-checkbox value="莫兰迪" label="莫兰迪" /><el-checkbox value="冷色调" label="冷色调" />
            <el-checkbox value="暖色调" label="暖色调" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="穿搭禁忌">
          <el-input v-model="form.fashionBan" type="textarea" :rows="3" placeholder="请输入你的穿搭禁忌，如：不适合紧身裙、不喜欢高领、腿粗不适合短裙等…" style="border: 2px solid #e6a23c; border-radius: 6px;" />
        </el-form-item>
        <el-form-item label="常驻城市">
          <el-input v-model="form.city" placeholder="用于获取实时天气和穿搭推荐（如：北京）。留空则默认使用「北京」">
            <template #append>默认：北京</template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveProfile" :loading="saving">保存档案</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo } from '../../api/user'
import { updateProfile } from '../../api/user'
import { setUser } from '../../utils/auth'
const saving = ref(false)
const form = reactive({ nickname: '', gender: '', height: 165, weight: 60, bodyType: '', skinTone: '', city: '', fashionBan: '' })
const stylePrefs = ref([])
const colorPrefs = ref([])
watch(stylePrefs, val => { form.stylePref = val.join(',') })
watch(colorPrefs, val => { form.colorPref = val.join(',') })
onMounted(async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200) {
      const u = res.data
      form.nickname = u.nickname || ''
      form.gender = u.gender || ''
      form.height = u.height || 165
      form.weight = u.weight || 60
      form.bodyType = u.bodyType || ''
      form.skinTone = u.skinTone || ''
      form.city = u.city || ''
      form.fashionBan = u.fashionBan || ''
      stylePrefs.value = u.stylePref ? u.stylePref.split(',') : []
      colorPrefs.value = u.colorPref ? u.colorPref.split(',') : []
    }
  } catch(e) {}
})
async function saveProfile() {
  saving.value = true
  try {
    const res = await updateProfile(form)
    if (res.code === 200) {
      ElMessage.success('档案已保存')
      const userRes = await getUserInfo()
      if (userRes.code === 200) setUser(userRes.data)
    }
  } catch(e) {}
  saving.value = false
}
</script>
<style scoped>
.profile-container { max-width: 900px; margin: 0 auto; }
.card-title { font-size: 18px; font-weight: bold; }
</style>
