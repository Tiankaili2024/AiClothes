<template>
  <div class="login-page">
    <LoginBackground />
    <div class="card-wrapper">
      <div class="login-card-base" style="width:540px;padding:30px 42px 18px">
        <h1 class="card-title">简搭 AI</h1>
        <p class="card-subtitle">发现属于你的日常风格</p>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="0" class="login-form" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="手机号 / 用户名" size="large" class="custom-input">
              <template #prefix><svg class="input-icon" viewBox="0 0 20 20" width="18" height="18" fill="none"><circle cx="10" cy="6" r="3.5" stroke="#99AEC2" stroke-width="1.3"/><path d="M3,18 C3,13 6.5,10 10,10 C13.5,10 17,13 17,18" stroke="#99AEC2" stroke-width="1.3" stroke-linecap="round"/></svg></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password class="custom-input">
              <template #prefix><svg class="input-icon" viewBox="0 0 20 20" width="18" height="18" fill="none"><rect x="4" y="9" width="12" height="9" rx="2" stroke="#99AEC2" stroke-width="1.3"/><path d="M6,9 V6 C6,3.5 7.5,2 10,2 C12.5,2 14,3.5 14,6 V9" stroke="#99AEC2" stroke-width="1.3" stroke-linecap="round"/><circle cx="10" cy="14" r="1.5" fill="#99AEC2"/></svg></template>
            </el-input>
          </el-form-item>
          <el-form-item><el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">登 录</el-button></el-form-item>
        </el-form>
        <div class="card-footer" style="margin-bottom:14px">
          <span>还没有账号？<router-link to="/register">立即注册</router-link></span>
          <span style="color:#C8D4DE">|</span>
          <router-link to="/forgot-password" style="color:#8A9EAE!important">忘记密码</router-link>
        </div>
        <div class="carousel-section">
          <div class="carousel-track">
            <div v-for="(item,i) in outfits" :key="i" class="carousel-slide" :style="{ transform:'translateX(-'+(activeSlide*100)+'%)' }">
              <div class="outfit-card" :style="{ background:item.bg }">
                <div class="outfit-content">
                  <span class="badge" :style="{ background:item.c }">{{ item.tag }}</span>
                  <div class="ot">{{ item.title }}</div>
                  <div class="os">{{ item.subtitle }}</div>
                  <div class="od">{{ item.desc }}</div>
                </div>
              </div>
            </div>
          </div>
          <div class="dots">
            <div v-for="(item,i) in outfits" :key="i" :class="['dot',{active:activeSlide===i}]" @click="activeSlide=i"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted, onUnmounted } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { login } from "../../api/auth"
import { setToken, setUser } from "../../utils/auth"
import LoginBackground from "../../components/LoginBackground.vue"
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username:"", password:"" })
const rules = { username:[{required:true,message:"请输入用户名",trigger:"blur"}], password:[{required:true,message:"请输入密码",trigger:"blur"}] }
const activeSlide = ref(0)
let timer = null
const outfits = [
  { tag:"轻运动", title:"轻运动出街", subtitle:"防晒外套·元气短裤", desc:"浅灰防晒外套+运动短裤·女生户外穿搭", bg:"linear-gradient(135deg,#E8F0F0,#DAE6E6)", c:"#7BA8A8" },
  { tag:"轻熟商务", title:"轻熟商务感", subtitle:"西装外套·高领针织", desc:"藏青西装+高领针织·男士商务穿搭", bg:"linear-gradient(135deg,#E0E4EA,#CCD2DB)", c:"#5A6A7E" },
  { tag:"夏日清爽", title:"夏日清爽公式", subtitle:"蓝白配色·干净显气质", desc:"浅蓝衬衫+白半裙·女生街拍穿搭", bg:"linear-gradient(135deg,#E8EDF8,#D4DCF0)", c:"#5A8AB0" },
  { tag:"周末松弛", title:"周末松弛感", subtitle:"卫衣叠穿·舒适有型", desc:"深灰连帽卫衣·男生休闲穿搭", bg:"linear-gradient(135deg,#E8E6E8,#D6D2D6)", c:"#6A6070" },
  { tag:"春日通勤", title:"春日通勤上新", subtitle:"轻盈风衣·利落裤装", desc:"卡其风衣+牛仔·女生通勤穿搭", bg:"linear-gradient(135deg,#F0EAE0,#E4DCCE)", c:"#A08868" },
]
function startCarousel() { stopCarousel(); timer=setInterval(()=>{activeSlide.value=(activeSlide.value+1)%outfits.length},3500) }
function stopCarousel() { if(timer){clearInterval(timer);timer=null} }
onMounted(()=>startCarousel())
onUnmounted(()=>stopCarousel())
async function handleLogin() {
  const valid=await formRef.value.validate().catch(()=>{}); if(!valid) return; loading.value=true
  try{const r=await login(form); if(r.code===200){setToken(r.data.token);setUser(r.data.user);ElMessage.success("登录成功");router.push("/outfit")}}catch(e){}
  loading.value=false
}
</script>
<style>
@import "../../assets/login-card.css";
.login-page { position:relative; width:100vw; height:100vh; overflow:hidden; background:#D4DEE6; font-family:-apple-system,BlinkMacSystemFont,"PingFang SC","Microsoft YaHei",sans-serif; }
.card-wrapper { position:absolute; inset:0; z-index:1; display:flex; justify-content:center; align-items:center; }
.input-icon { display:block; opacity:0.55; }
.carousel-section { border-top:1px solid rgba(200,212,222,0.35); padding-top:14px; }
.carousel-track { display:flex; overflow:hidden; border-radius:14px; }
.carousel-slide { flex:0 0 100%; transition:transform .6s cubic-bezier(0.25,0.46,0.45,0.94); }
.outfit-card { position:relative; height:130px; border-radius:14px; padding:16px 20px; display:flex; align-items:center; overflow:hidden; }
.outfit-content { position:relative; z-index:1; text-align:left; }
.badge { display:inline-block; padding:2px 12px; border-radius:20px; font-size:11px; color:#fff; font-weight:500; margin-bottom:6px; letter-spacing:1px; }
.ot { font-size:17px; font-weight:600; color:#3D4F5E; letter-spacing:1px; margin-bottom:2px; }
.os { font-size:13px; color:#5A7080; font-weight:500; }
.od { font-size:12px; color:#8A9EAE; margin-top:4px; }
.dots { display:flex; justify-content:center; gap:8px; margin-top:10px; }
.dot { width:7px; height:7px; border-radius:50%; background:#D0DCE6; cursor:pointer; transition:all .3s ease; }
.dot.active { width:22px; border-radius:4px; background:#8AB4D0; }
</style>
