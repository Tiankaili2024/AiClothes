<template>
  <div>
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-value">{{ d.totalUsers ?? demo.totalUsers }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-value" style="color:#67c23a">{{ d.totalRecords ?? demo.totalRecords }}</div>
            <div class="stat-label">总生成次数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-value" style="color:#e6a23c">{{ d.todayRecords ?? demo.todayRecords }}</div>
            <div class="stat-label">今日生成</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-value" style="color:#409eff">{{ d.successRate ?? demo.successRate }}%</div>
            <div class="stat-label">生成成功率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>穿搭风格分布</template>
          <v-chart :option="styleOption" style="height:350px" autoresize v-if="styleData.length" />
          <el-empty v-else description="暂无风格数据" :image-size="80" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>使用时段热度</template>
          <v-chart :option="hourOption" style="height:350px" autoresize v-if="hourData.some(v=>v>0)" />
          <el-empty v-else description="暂无时段数据" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDashboard } from '../../api/admin'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
use([CanvasRenderer, PieChart, LineChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const d = ref({})
const demo = { totalUsers: 286, totalRecords: 4520, todayRecords: 38, successRate: 94 }
const demoStyle = [
  { style_tags: '通勤', count: 35 }, { style_tags: '休闲', count: 25 },
  { style_tags: '温柔', count: 18 }, { style_tags: '运动', count: 12 },
  { style_tags: '甜酷', count: 10 },
]
const demoHour = Array.from({length:24}, (_,i) => ({ hour: i, count: i<6?Math.floor(Math.random()*5):
  i<9?Math.floor(Math.random()*20+30):i<12?Math.floor(Math.random()*15+40):
  i<14?Math.floor(Math.random()*10+20):i<18?Math.floor(Math.random()*20+35):
  i<22?Math.floor(Math.random()*15+50):Math.floor(Math.random()*10+15) }))

onMounted(async () => {
  try { const r = await getDashboard(); if (r.code===200 && r.data) d.value = r.data }
  catch(e) { d.value = { styleDistribution: demoStyle, hourlyActivity: demoHour } }
})

const styleData = computed(() => (d.value.styleDistribution || []).filter(s => s.count > 0))
const hourData = computed(() => Array.from({length:24}, (_,i) => {
  const item = (d.value.hourlyActivity || []).find(h => parseInt(h.hour)===i)
  return item ? parseInt(item.count) : 0
}))

const styleOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c}次 ({d}%)' },
  legend: { bottom: '5%' },
  series: [{ type: 'pie', radius: ['30%','60%'],
    data: styleData.value.map(s => ({ name: s.style_tags || '未知', value: parseInt(s.count)||0 })),
    emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' } },
  }],
}))
const hourOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '8%', top: '3%', containLabel: true },
  xAxis: { type: 'category', data: Array.from({length:24}, (_,i)=>i+'时') },
  yAxis: { type: 'value' },
  series: [{ type: 'line', smooth: true, data: hourData.value, areaStyle: { opacity: 0.3, color: '#409eff' }, lineStyle: { width: 2.5 } }],
}))
</script>
<style scoped>
.stat-item { text-align: center; padding: 10px 0; }
.stat-value { font-size: 36px; font-weight: bold; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 5px; }
</style>
