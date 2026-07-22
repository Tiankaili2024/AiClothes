<template>
  <div class="portrait-container">
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item"><div class="stat-value" style="color:#409eff">{{ portrait.totalCount || 0 }}</div><div class="stat-label">累计生成</div></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item"><div class="stat-value" style="color:#67c23a">{{ portrait.totalSchemes || 0 }}</div><div class="stat-label">累计方案</div></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item"><div class="stat-value" style="color:#e6a23c">{{ portrait.avgScore || 0 }}分</div><div class="stat-label">平均得分</div></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item"><div class="stat-value" style="color:#f56c6c">{{ portrait.favCount || 0 }}</div><div class="stat-label">收藏总数</div></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>我的风格分布</template>
          <div class="chart-wrap">
            <v-chart :option="styleOption" autoresize v-if="hasData" />
            <el-empty v-else description="暂无风格数据，请先生成穿搭方案" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>推荐方案</template>
          <div v-if="portrait.recommendedTemplates && portrait.recommendedTemplates.length">
            <div v-for="(tpl, i) in portrait.recommendedTemplates" :key="i" class="rec-item">
              <el-tag :type="['','success','warning','danger'][i % 4]" size="small" style="margin-right:8px">{{ tpl.style }}</el-tag>
              <span style="font-size:13px;color:#606266">{{ tpl.reason }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无推荐" />
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
import { PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
use([CanvasRenderer, PieChart, TitleComponent, TooltipComponent, LegendComponent])

const portrait = ref({})
const hasData = ref(false)

onMounted(async () => {
  try {
    const res = await request.get('/outfit/user-portrait')
    if (res.code === 200 && res.data) {
      portrait.value = res.data
      const dist = res.data.styleDistribution
      hasData.value = Array.isArray(dist) && dist.length > 0
    }
  } catch(e) {
    hasData.value = false
  }
})

const styleOption = computed(() => {
  const dist = portrait.value.styleDistribution || []
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: '5%' },
    series: [{
      type: 'pie',
      radius: ['30%', '60%'],
      avoidLabelOverlap: true,
      label: { show: true, formatter: '{b}\n{d}%' },
      data: dist.map(d => ({ name: d.style || d.style_tags || '未知', value: parseInt(d.count) || 0 })),
      emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' } },
    }],
  }
})
</script>
<style scoped>
.portrait-container { max-width: 1200px; margin: 0 auto; }
.stat-item { text-align: center; padding: 10px 0; }
.stat-value { font-size: 36px; font-weight: bold; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 5px; }
.rec-item { padding: 8px 0; border-bottom: 1px solid #f0f0f0; display: flex; align-items: center; }
.chart-wrap { width: 100%; height: 320px; }
</style>
