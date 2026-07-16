<template>
  <div class="login-page">
    <LoginBackground />
    <div class="card-wrapper">
      <div class="login-card-base">
        <h1 class="card-title">简搭 AI</h1>
        <p class="card-subtitle">创建你的账号</p>
        <el-form :model="form" :rules="rules" ref="formRef" label-width="0" class="login-form" @keyup.enter="handleRegister">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名(3-20位)" size="large" class="custom-input" prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="nickname">
            <el-input v-model="form.nickname" placeholder="昵称(选填)" size="large" class="custom-input" prefix-icon="Edit" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码(6-20位)" size="large" class="custom-input" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" size="large" class="custom-input" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleRegister">注 册</el-button>
          </el-form-item>
        </el-form>
        <div class="card-footer">已有账号？<router-link to="/login">立即登录</router-link></div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { register } from "../../api/auth"
import LoginBackground from "../../components/LoginBackground.vue"
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: "", nickname: "", password: "", confirmPassword: "" })
const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }, { min: 3, max: 20, message: "长度3-20位", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }, { min: 6, max: 20, message: "长度6-20位", trigger: "blur" }],
  confirmPassword: [{ required: true, message: "请确认密码", trigger: "blur" },
    { validator: (rule, value, cb) => value === form.password ? cb() : cb(new Error("两次密码不一致")), trigger: "blur" }],
}
async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => {})
  if (!valid) return; loading.value = true
  try { const r = await register({ username: form.username, password: form.password, nickname: form.nickname })
    if (r.code === 200) { ElMessage.success("注册成功"); router.push("/login") }
  } catch(e) {}
  loading.value = false
}
</script>
<style>
@import "../../assets/login-card.css";
.login-page { position:relative; width:100vw; height:100vh; overflow:hidden; background:#D4DEE6; font-family:-apple-system,BlinkMacSystemFont,"PingFang SC","Microsoft YaHei",sans-serif; }
.card-wrapper { position:absolute; inset:0; z-index:1; display:flex; justify-content:center; align-items:center; }
</style>
