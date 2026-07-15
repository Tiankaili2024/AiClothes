<template>
  <div>
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item"><div class="stat-value" style="color:#409eff">{{ data.topStyle || '--' }}</div><div class="stat-label">最热风格</div></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item"><div class="stat-value" style="color:#67c23a">{{ data.totalSchemes || 0 }}</div><div class="stat-label">总方案数</div></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item"><div class="stat-value" style="color:#e6a23c">{{ data.avgScore || 0 }}分</div><div class="stat-label">平均综合得分</div></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>男女用户偏好对比</template>
          <v-chart :option="genderOption" style="height:350px" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>时尚趋势</template>
          <v-chart :option="trendOption" style="height:350px" autoresize />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../../api/request'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
use([CanvasRenderer, BarChart, LineChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])
const data = ref({})
onMounted(async () => {
  try {
    const res = await request.get('/admin/portrait')
    if (res.code === 200) data.value = res.data
  } catch(e) {}
})
const genderOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['男', '女'] },
  xAxis: { type: 'category', data: (data.value.genderDiff || []).map(d => d.style) },
  yAxis: { type: 'value' },
  series: [
    { name: '男', type: 'bar', data: (data.value.genderDiff || []).map(d => d.male) },
    { name: '女', type: 'bar', data: (data.value.genderDiff || []).map(d => d.female) },
  ],
}))
const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: (data.value.trends || []).map(d => d.date || d.period) },
  yAxis: { type: 'value' },
  series: [{ type: 'line', smooth: true, data: (data.value.trends || []).map(d => d.count || d.value), areaStyle: { opacity: 0.3 } }],
}))
</script>
<style scoped>
.stat-item { text-align: center; padding: 10px 0; }
.stat-value { font-size: 36px; font-weight: bold; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 5px; }
</style>
