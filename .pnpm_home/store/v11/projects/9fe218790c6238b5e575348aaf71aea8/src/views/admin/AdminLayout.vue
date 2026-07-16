<template>
  <div>
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="数据概览" name="dashboard" />
      <el-tab-pane label="用户管理" name="users" />
      <el-tab-pane label="系统配置" name="config" />
      <el-tab-pane label="操作日志" name="logs" />
      <el-tab-pane label="全局画像" name="gportrait" />
    </el-tabs>
    <router-view />
  </div>
</template>
<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 从路径中提取当前 tab，例如 /admin/dashboard → dashboard
function getTabFromPath(path) {
  const m = path.match(/\/admin\/(\w+)/)
  return m ? m[1] : 'dashboard'
}

const activeTab = ref(getTabFromPath(route.fullPath))

// 监听路径变化同步顶部 tab
watch(() => route.fullPath, (val) => {
  activeTab.value = getTabFromPath(val)
})

// 点击 tab 时导航，同时左侧菜单会自动高亮
function handleTabClick(tab) {
  router.push('/admin/' + tab.props.name)
}
</script>
