<template>
  <div>
    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-value" style="color:#409eff">{{ data.topStyle || demo.topStyle }}</div>
            <div class="stat-label">最热风格</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-value" style="color:#67c23a">{{ data.totalSchemes ?? demo.totalSchemes }}</div>
            <div class="stat-label">总方案数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-value" style="color:#e6a23c">{{ data.avgScore ?? demo.avgScore }}分</div>
            <div class="stat-label">平均综合得分</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>男女用户偏好对比</template>
          <v-chart :option="genderOption" style="height:350px" autoresize v-if="hasChartData" />
          <el-empty v-else description="暂无对比数据" :image-size="80" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>时尚趋势</template>
          <v-chart :option="trendOption" style="height:350px" autoresize v-if="hasChartData" />
          <el-empty v-else description="暂无趋势数据" :image-size="80" />
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

// 降级演示数据（后端未运行时展示）
const demo = {
  topStyle: '通勤简约',
  totalSchemes: 1286,
  avgScore: 85,
  genderDiff: [
    { style: '通勤', male: 42, female: 58 },
    { style: '休闲', male: 55, female: 45 },
    { style: '温柔', male: 10, female: 90 },
    { style: '运动', male: 65, female: 35 },
    { style: '甜酷', male: 30, female: 70 },
  ],
  trends: [
    { period: '1月', value: 120 }, { period: '2月', value: 180 },
    { period: '3月', value: 240 }, { period: '4月', value: 300 },
    { period: '5月', value: 380 }, { period: '6月', value: 420 },
  ],
}

const hasChartData = computed(() => {
  const gd = data.value.genderDiff || demo.genderDiff
  const tr = data.value.trends || demo.trends
  return gd.length > 0 || tr.length > 0
})

onMounted(async () => {
  try {
    const res = await request.get('/admin/portrait')
    if (res.code === 200 && res.data) {
      data.value = res.data
    }
  } catch(e) {
    // 后端未运行，使用降级数据使页面可观
    data.value = demo
  }
})

const genderOption = computed(() => {
  const gd = data.value.genderDiff || demo.genderDiff
  const hasData = gd && gd.length > 0
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['男', '女'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '18%', top: '3%', containLabel: true },
    xAxis: { type: 'category', data: hasData ? gd.map(d => d.style || d.style_tags) : [] },
    yAxis: { type: 'value', min: 0, max: 100 },
    series: [
      { name: '男', type: 'bar', barWidth: '35%', data: hasData ? gd.map(d => d.male || d.male_percent || 0) : [] },
      { name: '女', type: 'bar', barWidth: '35%', data: hasData ? gd.map(d => d.female || d.female_percent || 0) : [] },
    ],
    color: ['#409eff', '#f56c6c'],
  }
})

const trendOption = computed(() => {
  const tr = data.value.trends || demo.trends
  const hasData = tr && tr.length > 0
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '8%', top: '3%', containLabel: true },
    xAxis: { type: 'category', data: hasData ? tr.map(d => d.date || d.period) : [] },
    yAxis: { type: 'value' },
    series: [{
      type: 'line', smooth: true,
      data: hasData ? tr.map(d => d.count || d.value) : [],
      areaStyle: { opacity: 0.25, color: '#409eff' },
      lineStyle: { width: 2.5, color: '#409eff' },
      itemStyle: { color: '#409eff' },
    }],
  }
})
</script>

<style scoped>
.stat-item { text-align: center; padding: 10px 0; }
.stat-value { font-size: 36px; font-weight: bold; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 5px; }
</style>
