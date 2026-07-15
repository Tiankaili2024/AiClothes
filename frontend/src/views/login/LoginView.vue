<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <el-icon :size="40" color="#409eff"><MagicStick /></el-icon>
        <h2>AI智能穿搭生成系统</h2>
        <p>登录您的账号</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" @click="handleLogin" :loading="loading" style="width: 100%">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        还没有账号？<router-link to="/register">立即注册</router-link> | <router-link to="/forgot-password" style="color:#909399">忘记密码</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/auth'
import { setToken, setUser } from '../../utils/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => {})
  if (!valid) return
  loading.value = true
  try {
    const res = await login(form)
    if (res.code === 200) {
      setToken(res.data.token)
      setUser(res.data.user)
      ElMessage.success('登录成功')
      router.push('/outfit')
    }
  } catch (e) {}
  loading.value = false
}
</script>

<style scoped>
.login-container {
  display: flex; justify-content: center; align-items: center;
  height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  background: #fff; border-radius: 16px; padding: 40px; width: 420px; box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.login-header { text-align: center; margin-bottom: 30px; }
.login-header h2 { margin: 10px 0 5px; color: #303133; }
.login-header p { color: #909399; font-size: 14px; }
.login-footer { text-align: center; margin-top: 20px; color: #909399; }
.login-footer a { color: #409eff; text-decoration: none; }
</style>
