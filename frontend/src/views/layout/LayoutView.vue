<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <div class="header-left">
        <el-icon :size="28" style="color: #409eff; margin-right: 10px"><MagicStick /></el-icon>
        <span class="logo-text">简搭 AI</span>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="32" :src="user.avatar" v-if="user.avatar" />
            <el-avatar :size="32" v-else icon="UserFilled" />
            <span style="margin-left: 8px">{{ user.nickname || user.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile"><el-icon><User /></el-icon>个人中心</el-dropdown-item>
              <el-dropdown-item command="password"><el-icon><Lock /></el-icon>修改密码</el-dropdown-item>
              <el-dropdown-item divided command="logout"><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-container style="height: calc(100vh - 60px)">
      <el-aside width="220px" class="layout-aside">
<<<<<<< HEAD
                <el-menu :default-active="route.path" router background-color="#1d1e1f" text-color="#bfcbd9" active-text-color="#409eff">
=======
                <el-menu :default-active="route.path" router background-color="#b4c9e3" text-color="#303133" active-text-color="#fff">
>>>>>>> 72c987a9749037dd86db97d59a877b31bb03efbc
          <el-menu-item index="/outfit">
            <el-icon><MagicStick /></el-icon><span>AI穿搭</span>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><Document /></el-icon><span>个人档案</span>
          </el-menu-item>
          <el-menu-item index="/portrait">
            <el-icon><TrendCharts /></el-icon><span>穿搭画像</span>
          </el-menu-item>
          <el-menu-item index="/history">
            <el-icon><Clock /></el-icon><span>穿搭记录</span>
          </el-menu-item>
          <el-menu-item index="/favorite">
            <el-icon><Star /></el-icon><span>我的收藏</span>
          </el-menu-item>
          <el-sub-menu index="admin" v-if="user.role === 1">
            <template #title>
              <el-icon><Setting /></el-icon><span>管理后台</span>
            </template>
            <el-menu-item index="/admin/dashboard"><el-icon><DataAnalysis /></el-icon>数据概览</el-menu-item>
            <el-menu-item index="/admin/users"><el-icon><User /></el-icon>用户管理</el-menu-item>
            <el-menu-item index="/admin/config"><el-icon><Tools /></el-icon>系统配置</el-menu-item>
            <el-menu-item index="/admin/logs"><el-icon><Document /></el-icon>操作日志</el-menu-item>
            <el-menu-item index="/admin/gportrait"><el-icon><Picture /></el-icon>全局画像</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
  <el-dialog v-model="pwdDialogVisible" title="修改密码" width="420px" :close-on-click-modal="false">
    <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="80px" @keyup.enter="submitPassword">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-20位新密码" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="pwdDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitPassword" :loading="pwdLoading">确认</el-button>
    </template>
  </el-dialog>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUser, setUser, removeToken, removeUser } from '../../utils/auth'
import { getUserInfo, updatePassword } from '../../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const user = ref(getUser() || { username: '\u7528\u6237', role: 0 })
const pwdDialogVisible = ref(false)
const pwdLoading = ref(false)
const pwdFormRef = ref(null)
const pwdForm = ref({ oldPassword: '', newPassword: '' })
const pwdRules = {
<<<<<<< HEAD
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, max: 20, message: '长度6-20位', trigger: 'blur' }],
=======
  oldPassword: [{ required: true, message: '\u8bf7\u8f93\u5165\u539f\u5bc6\u7801', trigger: 'blur' }],
  newPassword: [{ required: true, message: '\u8bf7\u8f93\u5165\u65b0\u5bc6\u7801', trigger: 'blur' }, { min: 6, max: 20, message: '\u957f\u5ea66-20\u4f4d', trigger: 'blur' }],
>>>>>>> 72c987a9749037dd86db97d59a877b31bb03efbc
}

onMounted(async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200) { user.value = res.data; setUser(res.data) }
  } catch(e) {}
})

function handleCommand(command) {
  if (command === 'profile') router.push('/profile')
  else if (command === 'password') pwdDialogVisible.value = true
  else if (command === 'logout') handleLogout()
  if (command === 'password') pwdDialogVisible.value = true
  else if (command === 'logout') handleLogout()
}

async function submitPassword() {
  const valid = await pwdFormRef.value?.validate().catch(() => {})
  if (!valid) return
  pwdLoading.value = true
  try {
    await updatePassword({ oldPassword: pwdForm.value.oldPassword, newPassword: pwdForm.value.newPassword })
<<<<<<< HEAD
    ElMessage.success('密码修改成功')
=======
    ElMessage.success('\u5bc6\u7801\u4fee\u6539\u6210\u529f')
>>>>>>> 72c987a9749037dd86db97d59a877b31bb03efbc
    pwdDialogVisible.value = false
    pwdForm.value = { oldPassword: '', newPassword: '' }
  } catch(e) {}
  pwdLoading.value = false
}

function handleLogout() {
  ElMessageBox.confirm('\u786e\u8ba4\u9000\u51fa\u767b\u5f55\uff1f', '\u63d0\u793a').then(() => { removeToken(); removeUser(); router.push('/login') }).catch(() => {})
}
</script>
<style scoped>
.layout-container { height: 100vh; }
.layout-header { display: flex; align-items: center; justify-content: space-between; background: #fff; border-bottom: 1px solid #e4e7ed; padding: 0 20px; height: 60px; }
.header-left { display: flex; align-items: center; }
.logo-text { font-size: 20px; font-weight: bold; color: #303133; }
.header-right { display: flex; align-items: center; }
.user-info { display: flex; align-items: center; cursor: pointer; }
<<<<<<< HEAD
.layout-aside { background-color: #1d1e1f; overflow-y: auto; }
.layout-main { background-color: #f5f7fa; padding: 20px; }
.el-menu { border-right: none; }
=======
.layout-aside { background-color: #b4c9e3; overflow-y: auto; }
.layout-main { background-color: #f9f7f3; padding: 20px; }

/* 侧边栏 el-menu 深度穿透（scoped 隔离，不污染其他页面） */
.layout-aside :deep(.el-menu),
.layout-aside :deep(.el-menu--vertical) {
  border-right: none !important;
}
.layout-aside :deep(.el-menu-item) {
  background-color: #b4c9e3 !important;
  color: #303133 !important;
}
.layout-aside :deep(.el-menu-item:hover) {
  background-color: #a4bdd8 !important;
}
.layout-aside :deep(.el-menu-item.is-active) {
  background-color: #93aed1 !important;
  color: #fff !important;
}
.layout-aside :deep(.el-menu-item .el-icon) {
  color: #303133 !important;
}
.layout-aside :deep(.el-sub-menu__title) {
  background-color: #b4c9e3 !important;
  color: #303133 !important;
}
.layout-aside :deep(.el-sub-menu__title:hover) {
  background-color: #a4bdd8 !important;
}
>>>>>>> 72c987a9749037dd86db97d59a877b31bb03efbc
</style>
