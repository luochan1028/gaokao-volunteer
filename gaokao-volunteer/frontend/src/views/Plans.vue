<template>
  <TechLayout>
    <div class="plans-page">
      <div class="container">
        <div class="page-header">
          <h2 class="heading-tech">我的志愿方案</h2>
          <p>管理和对比您保存的志愿填报方案</p>
        </div>

        <div class="plans-grid" v-if="plans.length > 0">
          <div
            v-for="plan in plans"
            :key="plan.id"
            class="plan-card glass-card tech-border"
            :class="{ 'is-default': plan.isDefault }"
          >
            <div class="card-header">
              <div class="plan-title">
                <h3>{{ plan.name }}</h3>
                <el-tag v-if="plan.isDefault" type="primary" size="small">默认方案</el-tag>
              </div>
              <div class="card-actions">
                <el-dropdown @command="(command) => handleAction(command, plan)">
                  <el-button size="small">更多 ▼</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="'view'">查看详情</el-dropdown-item>
                      <el-dropdown-item :command="'edit'">编辑名称</el-dropdown-item>
                      <el-dropdown-item :command="'copy'">复制方案</el-dropdown-item>
                      <el-dropdown-item v-if="!plan.isDefault" :command="'default'">设为默认</el-dropdown-item>
                      <el-dropdown-item :command="'delete'" divided>删除方案</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>

            <div class="card-content">
              <p class="plan-desc">{{ plan.description }}</p>
              <div class="plan-stats">
                <div class="stat-item">
                  <span class="stat-value">{{ plan.totalItems }}</span>
                  <span class="stat-label">志愿数</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value chong">{{ plan.chongCount }}</span>
                  <span class="stat-label">冲</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value wen">{{ plan.wenCount }}</span>
                  <span class="stat-label">稳</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value bao">{{ plan.baoCount }}</span>
                  <span class="stat-label">保</span>
                </div>
              </div>
            </div>

            <div class="card-footer">
              <span class="create-time">创建于 {{ formatDate(plan.createdAt) }}</span>
              <div class="footer-actions">
                <el-button size="small" type="primary" @click="viewPlan(plan)">查看</el-button>
                <el-button size="small" @click="generatePlanReport(plan)">生成报告</el-button>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <div class="empty-icon">📋</div>
          <h3>暂无志愿方案</h3>
          <p>您还没有保存任何志愿方案，快去创建一个吧！</p>
          <router-link to="/volunteer" class="btn-primary-tech">去创建</router-link>
        </div>
      </div>

      <el-dialog v-model="detailVisible" :title="selectedPlan?.name" width="900px">
        <div v-if="selectedPlan" class="plan-detail">
          <div class="detail-header">
            <div class="detail-info">
              <h3>{{ selectedPlan.name }}</h3>
              <p>{{ selectedPlan.description }}</p>
            </div>
            <div class="detail-stats">
              <div class="stat-card">
                <span class="stat-value">{{ selectedPlan.totalItems }}</span>
                <span class="stat-label">总志愿数</span>
              </div>
              <div class="stat-card">
                <span class="stat-value">{{ selectedPlan.chongCount }}/{{ selectedPlan.wenCount }}/{{ selectedPlan.baoCount }}</span>
                <span class="stat-label">冲/稳/保</span>
              </div>
            </div>
          </div>

          <div class="detail-content">
            <el-tabs v-model="detailTab" type="border-card">
              <el-tab-pane label="志愿列表" name="list">
                <el-table :data="selectedPlan.items" border>
                  <el-table-column label="序号" type="index" width="60" />
                  <el-table-column label="梯度" width="80">
                    <template #default="scope">
                      <el-tag :type="getGradientType(scope.row.gradientType)" size="small">
                        {{ getGradientLabel(scope.row.gradientType) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="院校" prop="collegeName" />
                  <el-table-column label="专业" prop="majorName" />
                  <el-table-column label="省份" prop="collegeProvince" width="80" />
                  <el-table-column label="录取概率" width="120">
                    <template #default="scope">
                      <div class="prob-bar">
                        <div class="prob-fill" :style="{ width: (scope.row.probability * 100) + '%' }"></div>
                      </div>
                      <span>{{ Number(scope.row.probability * 100).toFixed(2) }}%</span>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>

              <el-tab-pane label="风险评估" name="risk">
                <div class="risk-section">
                  <div class="risk-card" :class="getRiskClass(selectedPlan.riskLevel)">
                    <div class="risk-icon">{{ getRiskIcon(selectedPlan.riskLevel) }}</div>
                    <div class="risk-info">
                      <h3>{{ selectedPlan.riskLevel }}</h3>
                      <p>{{ selectedPlan.strategySuggestions }}</p>
                    </div>
                  </div>
                  <div class="risk-details">
                    <div class="detail-item">
                      <span>滑档概率</span>
                      <span>{{ Number(selectedPlan.slipProbability * 100).toFixed(2) }}%</span>
                    </div>
                    <div class="detail-item">
                      <span>浪费分数指数</span>
                      <span>{{ Number(selectedPlan.wasteScoreIndex * 100).toFixed(2) }}%</span>
                    </div>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>
        <template #footer>
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button type="primary" @click="generatePlanReport(selectedPlan)">生成报告</el-button>
        </template>
      </el-dialog>
    </div>
  </TechLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUserPlansApi, deletePlanApi, copyPlanApi, setDefaultPlanApi, updatePlanApi } from '@/api/volunteer'
import TechLayout from '@/components/TechLayout.vue'

const router = useRouter()

const plans = ref([])
const detailVisible = ref(false)
const selectedPlan = ref(null)
const detailTab = ref('list')

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const getGradientType = (type) => {
  const types = {
    'CHONG': 'danger',
    'WEN': 'warning',
    'BAO': 'success'
  }
  return types[type] || 'info'
}

const getGradientLabel = (type) => {
  const labels = {
    'CHONG': '冲',
    'WEN': '稳',
    'BAO': '保'
  }
  return labels[type] || type
}

const getRiskClass = (level) => {
  const classes = {
    '安全': 'risk-safe',
    '较安全': 'risk-safe',
    '中等风险': 'risk-medium',
    '高风险': 'risk-high'
  }
  return classes[level] || 'risk-medium'
}

const getRiskIcon = (level) => {
  const icons = {
    '安全': '✅',
    '较安全': '⚠️',
    '中等风险': '🔶',
    '高风险': '🚨'
  }
  return icons[level] || '⚠️'
}

const loadPlans = async () => {
  try {
    const response = await getUserPlansApi()
    plans.value = response.data.data || []
  } catch (error) {
    console.error('加载方案失败:', error)
  }
}

const viewPlan = (plan) => {
  selectedPlan.value = plan
  detailVisible.value = true
}

const handleAction = async (command, plan) => {
  switch (command) {
    case 'view':
      viewPlan(plan)
      break
    case 'edit':
      const newName = prompt('请输入新名称:', plan.name)
      if (newName) {
        try {
          await updatePlanApi(plan.id, newName, plan.description)
          await loadPlans()
        } catch (error) {
          console.error('修改名称失败:', error)
        }
      }
      break
    case 'copy':
      const copyName = prompt('请输入复制后的方案名称:', plan.name + ' (副本)')
      if (copyName) {
        try {
          await copyPlanApi(plan.id, copyName)
          await loadPlans()
        } catch (error) {
          console.error('复制方案失败:', error)
        }
      }
      break
    case 'default':
      try {
        await setDefaultPlanApi(plan.id)
        await loadPlans()
      } catch (error) {
        console.error('设为默认失败:', error)
      }
      break
    case 'delete':
      if (confirm(`确定要删除方案"${plan.name}"吗？`)) {
        try {
          await deletePlanApi(plan.id)
          await loadPlans()
        } catch (error) {
          console.error('删除方案失败:', error)
        }
      }
      break
  }
}

const generatePlanReport = (plan) => {
  alert('报告生成功能开发中')
}

onMounted(() => {
  loadPlans()
})
</script>

<style scoped>
.plans-page {
  padding: 40px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  font-size: 28px;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.plans-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.plan-card {
  padding: 25px;
  border-radius: 15px;
  border: 1px solid var(--color-border);
}

.plan-card.is-default {
  border-color: var(--color-accent-primary);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.plan-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.plan-title h3 {
  font-size: 18px;
  color: var(--color-text-primary);
  margin: 0;
}

.card-content {
  margin-bottom: 20px;
}

.plan-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 15px;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.plan-stats {
  display: flex;
  gap: 15px;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 10px;
  background: var(--color-bg-input);
  border-radius: 8px;
  border: 1px solid var(--color-border);
}

.stat-item .stat-value {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.stat-item .stat-value.chong {
  color: var(--color-accent-pink);
}

.stat-item .stat-value.wen {
  color: var(--color-accent-yellow);
}

.stat-item .stat-value.bao {
  color: var(--color-accent-green);
}

.stat-item .stat-label {
  display: block;
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 5px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid var(--color-border);
}

.create-time {
  font-size: 12px;
  color: var(--color-text-muted);
}

.footer-actions {
  display: flex;
  gap: 10px;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state h3 {
  font-size: 20px;
  color: var(--color-text-primary);
  margin-bottom: 10px;
}

.empty-state p {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 30px;
}

.plan-detail {
  padding: 10px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--color-border);
}

.detail-info h3 {
  font-size: 22px;
  color: var(--color-text-primary);
  margin-bottom: 5px;
}

.detail-info p {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.detail-stats {
  display: flex;
  gap: 20px;
}

.stat-card {
  padding: 15px 30px;
  background: var(--color-bg-input);
  border-radius: 10px;
  text-align: center;
  border: 1px solid var(--color-border);
}

.stat-card .stat-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: var(--color-accent-primary);
}

.stat-card .stat-label {
  display: block;
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 5px;
}

.detail-content {
  margin-top: 15px;
}

.prob-bar {
  height: 6px;
  background: var(--color-border);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 5px;
}

.prob-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-accent-primary), var(--color-accent-secondary));
  border-radius: 3px;
}

.risk-section {
  padding: 15px;
}

.risk-card {
  padding: 20px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.risk-safe {
  background: color-mix(in srgb, var(--color-accent-green) 8%, transparent);
  border-left: 5px solid var(--color-accent-green);
}

.risk-medium {
  background: color-mix(in srgb, var(--color-accent-yellow) 8%, transparent);
  border-left: 5px solid var(--color-accent-yellow);
}

.risk-high {
  background: color-mix(in srgb, var(--color-accent-pink) 8%, transparent);
  border-left: 5px solid var(--color-accent-pink);
}

.risk-icon {
  font-size: 32px;
}

.risk-info h3 {
  font-size: 18px;
  color: var(--color-text-primary);
  margin-bottom: 5px;
}

.risk-info p {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.risk-details {
  display: flex;
  gap: 20px;
}

.detail-item {
  flex: 1;
  padding: 15px;
  background: var(--color-bg-input);
  border-radius: 8px;
  text-align: center;
  border: 1px solid var(--color-border);
}

.detail-item span:first-child {
  display: block;
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 5px;
}

.detail-item span:last-child {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-accent-primary);
}

/* ========== Element Plus 暗色覆盖（el-tabs border-card / el-dropdown） ========== */
:deep(.el-tabs--border-card) {
  background: var(--color-bg-card);
  border-color: var(--color-border);
}

:deep(.el-tabs--border-card .el-tabs__header) {
  background: var(--color-bg-card);
  border-bottom-color: var(--color-border);
}

:deep(.el-tabs--border-card .el-tabs__item) {
  color: var(--color-text-secondary);
  transition: all 0.3s ease;
}

:deep(.el-tabs--border-card .el-tabs__item.is-active) {
  color: var(--color-accent-primary);
  background: var(--color-bg-card);
}

:deep(.el-tabs--border-card .el-tabs__item:hover) {
  color: var(--color-accent-primary);
}

:deep(.el-tabs--border-card .el-tabs__content) {
  background: transparent;
}

:deep(.el-tabs--border-card .el-tabs__nav-wrap::after) {
  background-color: var(--color-border);
}

:deep(.el-dropdown-menu) {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
}

:deep(.el-dropdown-menu__item) {
  color: var(--color-text-secondary);
}

:deep(.el-dropdown-menu__item:not(.is-disabled):hover) {
  background: color-mix(in srgb, var(--color-accent-primary) 10%, transparent);
  color: var(--color-accent-primary);
}

:deep(.el-dropdown-menu__item--divided) {
  border-top-color: var(--color-border);
}

@media (max-width: 992px) {
  .plans-grid {
    grid-template-columns: 1fr;
  }

  .detail-header {
    flex-direction: column;
    gap: 15px;
  }

  .detail-stats {
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .plans-grid {
    grid-template-columns: repeat(1, 1fr);
  }

  .detail-header {
    flex-direction: column;
    gap: 15px;
  }

  .plan-stats {
    flex-wrap: wrap;
  }

  .stat-item {
    min-width: calc(50% - 10px);
  }

  .risk-details {
    flex-direction: column;
  }

  :deep(.el-dialog) {
    width: 95% !important;
  }
}
</style>
