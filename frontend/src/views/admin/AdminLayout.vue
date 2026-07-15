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
const activeTab = ref(route.name || 'dashboard')
watch(() => route.name, val => { activeTab.value = val || 'dashboard' })
function handleTabClick(tab) { router.push('/admin/' + tab.props.name) }
</script>