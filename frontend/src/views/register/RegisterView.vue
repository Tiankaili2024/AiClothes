<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <el-icon :size="40" color="#409eff"><MagicStick /></el-icon>
        <h2>创建账号</h2>
        <p>注册AI智能穿搭系统</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名(3-20位)" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称(选填)" size="large" prefix-icon="Edit" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码(6-20位)" size="large" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" size="large" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" @click="handleRegister" :loading="loading" style="width: 100%">注 册</el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../../api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', nickname: '', password: '', confirmPassword: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, max: 20, message: '长度3-20位', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 20, message: '长度6-20位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (rule, value, cb) => value === form.password ? cb() : cb(new Error('两次密码不一致')), trigger: 'blur' }],
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => {})
  if (!valid) return
  loading.value = true
  try {
    const res = await register({ username: form.username, password: form.password, nickname: form.nickname })
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    }
  } catch (e) {}
  loading.value = false
}
</script>

<style scoped>
.register-container {
  display: flex; justify-content: center; align-items: center;
  height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.register-card {
  background: #fff; border-radius: 16px; padding: 40px; width: 420px; box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.register-header { text-align: center; margin-bottom: 30px; }
.register-header h2 { margin: 10px 0 5px; color: #303133; }
.register-header p { color: #909399; font-size: 14px; }
.register-footer { text-align: center; margin-top: 20px; color: #909399; }
.register-footer a { color: #409eff; text-decoration: none; }
</style>
