<template>
  <div>
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-value">{{ data.totalUsers || 0 }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-value" style="color:#67c23a">{{ data.totalRecords || 0 }}</div>
            <div class="stat-label">总生成次数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-value" style="color:#e6a23c">{{ data.todayRecords || 0 }}</div>
            <div class="stat-label">今日生成</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-value" style="color:#409eff">{{ data.successRate || 0 }}%</div>
            <div class="stat-label">生成成功率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>穿搭风格分布</template>
          <v-chart :option="styleOption" style="height:350px" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>使用时段热度</template>
          <v-chart :option="hourOption" style="height:350px" autoresize />
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
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'

use([CanvasRenderer, PieChart, LineChart, TitleComponent, TooltipComponent, LegendComponent])

const data = ref({})

onMounted(async () => {
  try {
    const res = await getDashboard()
    if (res.code === 200) data.value = res.data
  } catch (e) {}
})

const styleOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '5%' },
  series: [{
    type: 'pie',
    radius: ['30%', '60%'],
    data: (data.value.styleDistribution || []).map(item => ({
      name: item.style_tags || '未知',
      value: parseInt(item.count) || 0,
    })),
    emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' } },
  }],
}))

const hourOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: Array.from({length: 24}, (_,i) => i + '时') },
  yAxis: { type: 'value' },
  series: [{
    type: 'line',
    smooth: true,
    data: Array.from({length: 24}, (_, i) => {
      const item = (data.value.hourlyActivity || []).find(h => parseInt(h.hour) === i)
      return item ? parseInt(item.count) : 0
    }),
    areaStyle: { opacity: 0.3 },
  }],
}))
</script>

<style scoped>
.stat-item { text-align: center; padding: 10px 0; }
.stat-value { font-size: 36px; font-weight: bold; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 5px; }
</style>
