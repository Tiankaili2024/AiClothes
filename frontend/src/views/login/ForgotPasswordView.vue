<template>
  <div class="forgot-container">
    <div class="forgot-card">
      <div class="forgot-header">
        <el-icon :size="40" color="#409eff"><MagicStick /></el-icon>
        <h2>找回密码</h2>
        <p>通过密保问题重置密码</p>
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
          <el-button type="primary" size="large" @click="handleRegister" :loading="loading" style="width: 100%">重置密码</el-button>
        </el-form-item>
      </el-form>
      <div class="forgot-footer">
        记起密码了？<router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

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
    const res = await forgot({ username: form.username, password: form.password, nickname: form.nickname })
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    }
  } catch (e) {}
  loading.value = false
}
</script>

<style scoped>
.forgot-container {
  display: flex; justify-content: center; align-items: center;
  height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.forgot-card {
  background: #fff; border-radius: 16px; padding: 40px; width: 420px; box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.forgot-header { text-align: center; margin-bottom: 30px; }
.forgot-header h2 { margin: 10px 0 5px; color: #303133; }
.forgot-header p { color: #909399; font-size: 14px; }
.forgot-footer { text-align: center; margin-top: 20px; color: #909399; }
.forgot-footer a { color: #409eff; text-decoration: none; }
</style>
