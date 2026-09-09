<template>
  <TechLayout>
    <div class="risk-assessment-page">
    <div class="page-header">
      <h1 class="heading-tech">志愿风险评估</h1>
      <p class="subtitle">RISK ASSESSMENT DASHBOARD · 风险量化与预警仪表盘</p>
    </div>

    <div v-loading="loading">
      <!-- 风险仪表盘 -->
      <el-row :gutter="20" v-if="dashboard">
        <el-col :span="8">
          <el-card shadow="hover" class="dashboard-card" :class="levelClass">
            <div class="risk-level-display">
              <div class="risk-number" :style="{ color: levelColor }">{{ riskLabel }}</div>
              <div class="risk-desc">整体风险等级</div>
              <div class="risk-stats">
                <span>风险项: {{ dashboard.riskCount }}</span>
                <span>高危: {{ dashboard.highRiskCount }}</span>
                <span>中危: {{ dashboard.mediumRiskCount }}</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card shadow="hover" class="dashboard-card">
            <template #header>滑档概率</template>
            <div class="slip-prob">
              <el-progress :percentage="Number(dashboard.slipProbability || 0).toFixed(2)" :color="slipColor" :stroke-width="16" type="dashboard" />
              <div class="slip-text">{{ slipText }}</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card shadow="hover" class="dashboard-card">
            <template #header>梯度分析</template>
            <div class="gradient-bar">
              <div class="gradient-seg chong" :style="{ width: dashboard.gradient.chongPct + '%' }">
                <span>冲 {{ Number(dashboard.gradient.chongPct || 0).toFixed(2) }}%</span>
              </div>
              <div class="gradient-seg wen" :style="{ width: dashboard.gradient.wenPct + '%' }">
                <span>稳 {{ Number(dashboard.gradient.wenPct || 0).toFixed(2) }}%</span>
              </div>
              <div class="gradient-seg bao" :style="{ width: dashboard.gradient.baoPct + '%' }">
                <span>保 {{ Number(dashboard.gradient.baoPct || 0).toFixed(2) }}%</span>
              </div>
            </div>
            <el-divider />
            <div class="ideal-gradient">
              <div class="ideal-label">建议梯度：冲20% · 稳50% · 保30%</div>
              <div class="gradient-counts">
                <el-tag type="danger">冲: {{ dashboard.gradient.chong }}</el-tag>
                <el-tag type="warning">稳: {{ dashboard.gradient.wen }}</el-tag>
                <el-tag type="success">保: {{ dashboard.gradient.bao }}</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 风险明细列表 -->
      <el-card shadow="hover" style="margin-top:20px" v-if="dashboard">
        <template #header>
          <div style="display:flex;align-items:center;justify-content:space-between">
            <span style="font-weight:bold">风险明细</span>
            <el-button type="primary" size="small" @click="goToVolunteer">调整志愿表</el-button>
          </div>
        </template>

        <el-empty v-if="!dashboard.riskItems.length" description="暂无风险项，志愿配置合理" />

        <el-collapse v-else>
          <el-collapse-item v-for="(item, idx) in dashboard.riskItems" :key="idx" :name="idx">
            <template #title>
              <div class="risk-item-header">
                <el-tag :type="levelTagType(item.level)" size="small">{{ levelText(item.level) }}</el-tag>
                <span class="risk-title">{{ item.title }}</span>
                <span class="risk-desc-inline">{{ item.description }}</span>
              </div>
            </template>
            <div class="risk-detail">
              <p><strong>风险描述：</strong>{{ item.description }}</p>
              <p><strong>调整建议：</strong>{{ item.suggestion }}</p>
            </div>
          </el-collapse-item>
        </el-collapse>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-bar" v-if="dashboard">
        <el-button type="primary" size="large" @click="goToSubmit" :disabled="dashboard.overallLevel === 'HIGH'">
          前往模拟提交
        </el-button>
        <el-button size="large" @click="refresh">重新评估</el-button>
      </div>

      <el-empty v-if="!loading && !dashboard" description="请先在志愿表中添加志愿" />
    </div>
    </div>
  </TechLayout>
</template>

<script setup>
import TechLayout from '@/components/TechLayout.vue'
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { assessRiskApi } from '@/api/risk'

const router = useRouter()
const loading = ref(false)
const dashboard = ref(null)

const riskLabel = computed(() => {
  if (!dashboard.value) return ''
  const labels = { LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险', EMPTY: '无志愿' }
  return labels[dashboard.value.overallLevel] || ''
})

const levelColor = computed(() => {
  if (!dashboard.value) return '#333'
  const colors = { LOW: '#67c23a', MEDIUM: '#e6a23c', HIGH: '#f56c6c', EMPTY: '#909399' }
  return colors[dashboard.value.overallLevel] || '#333'
})

const levelClass = computed(() => ({
  'risk-low': dashboard.value?.overallLevel === 'LOW',
  'risk-medium': dashboard.value?.overallLevel === 'MEDIUM',
  'risk-high': dashboard.value?.overallLevel === 'HIGH'
}))

const slipColor = computed(() => {
  if (!dashboard.value) return '#67c23a'
  const p = dashboard.value.slipProbability
  if (p >= 60) return '#f56c6c'
  if (p >= 30) return '#e6a23c'
  return '#67c23a'
})

const slipText = computed(() => {
  if (!dashboard.value) return ''
  const p = dashboard.value.slipProbability
  if (p >= 60) return '滑档风险高，建议增加保底志愿'
  if (p >= 30) return '有一定滑档风险，注意梯度'
  return '滑档风险低，志愿梯度合理'
})

function levelText(level) {
  return { HIGH: '高危', MEDIUM: '中危', LOW: '低危' }[level] || level
}

function levelTagType(level) {
  return { HIGH: 'danger', MEDIUM: 'warning', LOW: 'success' }[level] || 'info'
}

async function refresh() {
  loading.value = true
  try {
    const res = await assessRiskApi()
    if (res.data.code === 200) {
      dashboard.value = res.data.data
    }
  } catch (e) {
    console.error('评估失败', e)
  } finally {
    loading.value = false
  }
}

function goToVolunteer() {
  router.push('/volunteer')
}

function goToSubmit() {
  router.push('/submit')
}

onMounted(() => refresh())
</script>

<style scoped>
.risk-assessment-page { padding: 20px; max-width: 1200px; margin: 0 auto; }
.page-header { text-align: center; margin-bottom: 24px; }
.page-header h1 { font-size: 32px; margin-bottom: 8px; font-family: var(--font-heading); }
.page-header .subtitle { color: var(--color-text-muted); font-size: 13px; font-family: var(--font-mono); letter-spacing: 1px; }
.dashboard-card { text-align: center; }
.risk-level-display { padding: 10px 0; }
.risk-number { font-size: 36px; font-weight: bold; margin-bottom: 8px; font-family: var(--font-heading); }
.risk-desc { color: var(--color-text-muted); font-size: 14px; margin-bottom: 12px; }
.risk-stats { display: flex; justify-content: center; gap: 16px; font-size: 13px; color: var(--color-text-secondary); }
.slip-prob { text-align: center; padding: 10px 0; }
.slip-text { margin-top: 10px; color: var(--color-text-secondary); font-size: 13px; }
.gradient-bar { display: flex; height: 40px; border-radius: 6px; overflow: hidden; }
.gradient-seg { display: flex; align-items: center; justify-content: center; color: #fff; font-size: 13px; font-weight: bold; }
.gradient-seg.chong { background: linear-gradient(135deg, #ff2d92, #ff6b6b); }
.gradient-seg.wen { background: linear-gradient(135deg, #ffaa00, #ffd700); }
.gradient-seg.bao { background: linear-gradient(135deg, #00ff9d, #00d4ff); }
.ideal-gradient { text-align: center; }
.ideal-label { color: var(--color-text-muted); font-size: 13px; margin-bottom: 8px; }
.gradient-counts { display: flex; justify-content: center; gap: 8px; }
.risk-item-header { display: flex; align-items: center; gap: 8px; width: 100%; }
.risk-title { font-weight: bold; }
.risk-desc-inline { color: var(--color-text-muted); font-size: 13px; }
.risk-detail { padding: 0 16px; }
.risk-detail p { margin: 8px 0; }
.action-bar { text-align: center; margin-top: 24px; }
.risk-low { border-top: 4px solid var(--color-accent-green); }
.risk-medium { border-top: 4px solid var(--color-accent-yellow); }
.risk-high { border-top: 4px solid var(--color-accent-pink); }
</style>
