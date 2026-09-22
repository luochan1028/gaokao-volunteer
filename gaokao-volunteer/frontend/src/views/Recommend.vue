<template>
  <TechLayout>
    <div class="recommend-page">
      <main class="main-content">
        <div class="container">
          <div class="guide-section">
            <div class="guide-step" :class="{ active: currentStep === 1, completed: currentStep > 1 }">
              <div class="step-icon">1</div>
              <span>基本信息</span>
            </div>
            <div class="guide-arrow">→</div>
            <div class="guide-step" :class="{ active: currentStep === 2, completed: currentStep > 2 }">
              <div class="step-icon">2</div>
              <span>筛选偏好</span>
            </div>
            <div class="guide-arrow">→</div>
            <div class="guide-step" :class="{ active: currentStep === 3, completed: currentStep > 3 }">
              <div class="step-icon">3</div>
              <span>生成方案</span>
            </div>
          </div>

          <div class="form-section glass-card tech-border" v-if="currentStep <= 2">
            <div v-if="currentStep === 1" class="step-content">
              <h2 class="heading-tech">第一步：填写基本信息</h2>
              <p>请填写您的考试信息，系统将根据位次为您推荐志愿</p>

              <el-form :model="formData" label-width="120px" class="info-form">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="所在省份" required>
                      <el-select v-model="formData.province" placeholder="请选择省份">
                        <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="考试年份" required>
                      <el-select v-model="formData.year" placeholder="请选择年份">
                        <el-option v-for="y in years" :key="y" :label="y" :value="y" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="理科/文科" required>
                      <el-select v-model="formData.scienceOrArts" placeholder="请选择">
                        <el-option label="理科" value="SCIENCE" />
                        <el-option label="文科" value="ARTS" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="选考科目">
                      <el-select v-model="formData.selectedSubjects" multiple placeholder="请选择选考科目">
                        <el-option label="物理" value="物理" />
                        <el-option label="化学" value="化学" />
                        <el-option label="生物" value="生物" />
                        <el-option label="历史" value="历史" />
                        <el-option label="政治" value="政治" />
                        <el-option label="地理" value="地理" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="高考分数">
                      <el-input v-model.number="formData.score" placeholder="输入分数" />
                      <span class="form-tip">若知道位次可直接填写</span>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="全省位次">
                      <el-input v-model.number="formData.rank" placeholder="输入位次" />
                      <span class="form-tip">位次优先于分数计算</span>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>

              <div class="form-actions">
                <el-button type="primary" @click="nextStep" :disabled="!validateStep1">下一步</el-button>
              </div>
            </div>

            <div v-if="currentStep === 2" class="step-content">
              <h2 class="heading-tech">第二步：设置筛选偏好</h2>
              <p>根据您的偏好设置筛选条件，系统将为您生成更精准的推荐</p>

              <div class="preference-section">
                <div class="preference-card">
                  <h3>院校类型</h3>
                  <div class="preference-options">
                    <el-checkbox v-model="filterData.include985">985院校</el-checkbox>
                    <el-checkbox v-model="filterData.include211">211院校</el-checkbox>
                    <el-checkbox v-model="filterData.includeDoubleFirstClass">双一流院校</el-checkbox>
                    <el-checkbox v-model="filterData.includePrivate">民办院校</el-checkbox>
                  </div>
                </div>

                <div class="preference-card">
                  <h3>目标省份</h3>
                  <el-select v-model="filterData.preferredProvinces" multiple placeholder="请选择偏好省份" class="province-select">
                    <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
                  </el-select>
                </div>

                <div class="preference-card">
                  <h3>偏好专业</h3>
                  <el-select v-model="filterData.preferredMajors" multiple placeholder="请选择偏好专业" class="major-select">
                    <el-option v-for="m in majorCategories" :key="m.value" :label="m.label" :value="m.label" />
                  </el-select>
                </div>

                <div class="preference-card">
                  <h3>志愿数量</h3>
                  <el-row :gutter="20">
                    <el-col :span="8">
                      <label>冲（高风险）</label>
                      <el-input-number v-model="filterData.chongCount" :min="0" :max="20" />
                    </el-col>
                    <el-col :span="8">
                      <label>稳（中风险）</label>
                      <el-input-number v-model="filterData.wenCount" :min="0" :max="20" />
                    </el-col>
                    <el-col :span="8">
                      <label>保（低风险）</label>
                      <el-input-number v-model="filterData.baoCount" :min="0" :max="20" />
                    </el-col>
                  </el-row>
                </div>

                <div class="preference-card">
                  <h3>排序方式</h3>
                  <el-radio-group v-model="filterData.sortBy">
                    <el-radio label="院校优先">院校优先</el-radio>
                    <el-radio label="专业优先">专业优先</el-radio>
                  </el-radio-group>
                </div>
              </div>

              <div class="form-actions">
                <el-button @click="prevStep">上一步</el-button>
                <el-button type="primary" @click="generateRecommendations">生成推荐方案</el-button>
              </div>
            </div>
          </div>

          <div class="result-section" v-if="currentStep === 3 && recommendationResult">
            <div class="result-header">
              <h2 class="heading-tech">智能推荐结果</h2>
              <p>基于您的位次和偏好，系统为您生成以下志愿方案</p>
            </div>

            <div class="risk-summary">
              <div class="risk-card glass-card tech-border" :class="getRiskClass(recommendationResult.riskAssessment.riskLevel)">
                <div class="risk-icon">{{ getRiskIcon(recommendationResult.riskAssessment.riskLevel) }}</div>
                <div class="risk-info">
                  <h3>{{ recommendationResult.riskAssessment.riskLevel }}</h3>
                  <p>{{ recommendationResult.riskAssessment.suggestions }}</p>
                </div>
              </div>

              <div class="stats-grid">
                <div class="stat-item glass-card">
                  <span class="stat-value">{{ Number(recommendationResult.riskAssessment.slipProbability * 100).toFixed(2) }}%</span>
                  <span class="stat-label">滑档概率</span>
                </div>
                <div class="stat-item glass-card">
                  <span class="stat-value">{{ Number(recommendationResult.riskAssessment.wasteScoreIndex * 100).toFixed(2) }}%</span>
                  <span class="stat-label">浪费分数指数</span>
                </div>
                <div class="stat-item glass-card">
                  <span class="stat-value">{{ recommendationResult.riskAssessment.totalRecommended }}</span>
                  <span class="stat-label">推荐总数</span>
                </div>
              </div>
            </div>

            <div class="gradient-tabs glass-card tech-border">
              <el-tabs v-model="activeTab" type="border-card">
                <el-tab-pane name="chong"><template #label>🎯 冲（{{ recommendationResult.chongList.length }}所）</template>
                  <div class="volunteer-list">
                    <div
                      v-for="item in recommendationResult.chongList"
                      :key="item.collegeId + '-' + item.majorId"
                      class="volunteer-item"
                    >
                      <div class="item-rank">{{ item.priorityOrder }}</div>
                      <div class="item-info">
                        <div class="item-header">
                          <span class="college-name">{{ item.collegeName }}</span>
                          <div class="item-badges">
                            <el-tag v-if="item.is985" type="danger" size="small">985</el-tag>
                            <el-tag v-if="item.is211" type="warning" size="small">211</el-tag>
                            <el-tag v-if="item.isDoubleFirstClass" type="primary" size="small">双一流</el-tag>
                          </div>
                        </div>
                        <div class="item-detail">
                          <span>{{ item.collegeProvince }}·{{ item.collegeCity }}</span>
                          <span>专业：{{ item.majorName }}</span>
                        </div>
                        <div class="item-scores">
                          <span>去年最低分：{{ Number(item.lastYearMinScore || 0).toFixed(2) }}</span>
                          <span>最低位次：{{ Number(item.lastYearMinRank || 0).toFixed(2) }}</span>
                        </div>
                      </div>
                      <div class="item-probability">
                        <div class="probability-bar">
                          <div class="probability-fill" :style="{ width: (item.probability * 100) + '%' }"></div>
                        </div>
                        <span class="probability-text">{{ Number(item.probability * 100).toFixed(2) }}%</span>
                        <el-tag size="small" type="danger">高风险</el-tag>
                      </div>
                      <div class="item-actions">
                        <el-button size="small" @click="addToVolunteer(item)">加入志愿</el-button>
                      </div>
                    </div>
                  </div>
                </el-tab-pane>

                <el-tab-pane name="wen"><template #label>✅ 稳（{{ recommendationResult.wenList.length }}所）</template>
                  <div class="volunteer-list">
                    <div
                      v-for="item in recommendationResult.wenList"
                      :key="item.collegeId + '-' + item.majorId"
                      class="volunteer-item"
                    >
                      <div class="item-rank">{{ item.priorityOrder }}</div>
                      <div class="item-info">
                        <div class="item-header">
                          <span class="college-name">{{ item.collegeName }}</span>
                          <div class="item-badges">
                            <el-tag v-if="item.is985" type="danger" size="small">985</el-tag>
                            <el-tag v-if="item.is211" type="warning" size="small">211</el-tag>
                            <el-tag v-if="item.isDoubleFirstClass" type="primary" size="small">双一流</el-tag>
                          </div>
                        </div>
                        <div class="item-detail">
                          <span>{{ item.collegeProvince }}·{{ item.collegeCity }}</span>
                          <span>专业：{{ item.majorName }}</span>
                        </div>
                        <div class="item-scores">
                          <span>去年最低分：{{ Number(item.lastYearMinScore || 0).toFixed(2) }}</span>
                          <span>最低位次：{{ Number(item.lastYearMinRank || 0).toFixed(2) }}</span>
                        </div>
                      </div>
                      <div class="item-probability">
                        <div class="probability-bar">
                          <div class="probability-fill" :style="{ width: (item.probability * 100) + '%' }"></div>
                        </div>
                        <span class="probability-text">{{ Number(item.probability * 100).toFixed(2) }}%</span>
                        <el-tag size="small" type="warning">中风险</el-tag>
                      </div>
                      <div class="item-actions">
                        <el-button size="small" @click="addToVolunteer(item)">加入志愿</el-button>
                      </div>
                    </div>
                  </div>
                </el-tab-pane>

                <el-tab-pane name="bao"><template #label>🛡️ 保（{{ recommendationResult.baoList.length }}所）</template>
                  <div class="volunteer-list">
                    <div
                      v-for="item in recommendationResult.baoList"
                      :key="item.collegeId + '-' + item.majorId"
                      class="volunteer-item"
                    >
                      <div class="item-rank">{{ item.priorityOrder }}</div>
                      <div class="item-info">
                        <div class="item-header">
                          <span class="college-name">{{ item.collegeName }}</span>
                          <div class="item-badges">
                            <el-tag v-if="item.is985" type="danger" size="small">985</el-tag>
                            <el-tag v-if="item.is211" type="warning" size="small">211</el-tag>
                            <el-tag v-if="item.isDoubleFirstClass" type="primary" size="small">双一流</el-tag>
                          </div>
                        </div>
                        <div class="item-detail">
                          <span>{{ item.collegeProvince }}·{{ item.collegeCity }}</span>
                          <span>专业：{{ item.majorName }}</span>
                        </div>
                        <div class="item-scores">
                          <span>去年最低分：{{ Number(item.lastYearMinScore || 0).toFixed(2) }}</span>
                          <span>最低位次：{{ Number(item.lastYearMinRank || 0).toFixed(2) }}</span>
                        </div>
                      </div>
                      <div class="item-probability">
                        <div class="probability-bar">
                          <div class="probability-fill" :style="{ width: (item.probability * 100) + '%' }"></div>
                        </div>
                        <span class="probability-text">{{ Number(item.probability * 100).toFixed(2) }}%</span>
                        <el-tag size="small" type="success">低风险</el-tag>
                      </div>
                      <div class="item-actions">
                        <el-button size="small" @click="addToVolunteer(item)">加入志愿</el-button>
                      </div>
                    </div>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>

            <div class="result-actions">
              <el-button @click="currentStep = 2">重新调整</el-button>
              <el-button type="primary" @click="saveAllToVolunteer">全部加入志愿列表</el-button>
              <el-button type="success" @click="saveAsPlan">保存为方案</el-button>
            </div>
          </div>
        </div>
      </main>
    </div>
  </TechLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import TechLayout from '@/components/TechLayout.vue'
import { getAllProvincesApi } from '@/api/college'
import { generateRecommendationsApi } from '@/api/recommend'
import { addToVolunteerListApi } from '@/api/volunteer'

const router = useRouter()

const currentStep = ref(1)
const provinces = ref([])
const years = ref(['2024'])
const activeTab = ref('chong')

const formData = reactive({
  province: '',
  year: '2024',
  scienceOrArts: '',
  selectedSubjects: [],
  score: null,
  rank: null
})

const filterData = reactive({
  include985: true,
  include211: true,
  includeDoubleFirstClass: true,
  includePrivate: false,
  preferredProvinces: [],
  preferredMajors: [],
  chongCount: 6,
  wenCount: 8,
  baoCount: 6,
  sortBy: '院校优先'
})

const recommendationResult = ref(null)

const majorCategories = [
  { label: '哲学', value: 'PHILOSOPHY' },
  { label: '经济学', value: 'ECONOMICS' },
  { label: '法学', value: 'LAW' },
  { label: '教育学', value: 'EDUCATION' },
  { label: '文学', value: 'LITERATURE' },
  { label: '历史学', value: 'HISTORY' },
  { label: '理学', value: 'SCIENCE' },
  { label: '工学', value: 'ENGINEERING' },
  { label: '农学', value: 'AGRICULTURE' },
  { label: '医学', value: 'MEDICINE' },
  { label: '管理学', value: 'MANAGEMENT' },
  { label: '艺术学', value: 'ART' }
]

const validateStep1 = computed(() => {
  return formData.province && formData.year && formData.scienceOrArts && (formData.score || formData.rank)
})

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

const nextStep = () => {
  if (validateStep1.value) {
    currentStep.value = 2
  }
}

const prevStep = () => {
  currentStep.value = 1
}

const generateRecommendations = async () => {
  try {
    const data = {
      ...formData,
      ...filterData
    }
    const response = await generateRecommendationsApi(data)
    recommendationResult.value = response.data.data
    currentStep.value = 3
  } catch (error) {
    console.error('生成推荐失败:', error)
    alert('生成推荐失败，请稍后重试')
  }
}

const addToVolunteer = async (item) => {
  try {
    await addToVolunteerListApi(item.collegeId, item.majorId, item.majorName)
    alert('已加入志愿列表')
  } catch (error) {
    console.error('加入志愿失败:', error)
    alert('加入志愿失败')
  }
}

const saveAllToVolunteer = async () => {
  try {
    const allItems = [
      ...recommendationResult.value.chongList,
      ...recommendationResult.value.wenList,
      ...recommendationResult.value.baoList
    ]
    for (const item of allItems) {
      await addToVolunteerListApi(item.collegeId, item.majorId, item.majorName)
    }
    alert('全部已加入志愿列表')
  } catch (error) {
    console.error('批量加入失败:', error)
    alert('批量加入失败')
  }
}

const saveAsPlan = () => {
  router.push('/volunteer')
}

onMounted(async () => {
  try {
    const provincesRes = await getAllProvincesApi()
    provinces.value = provincesRes.data.data || ['北京', '上海', '广东', '浙江', '江苏', '山东', '四川', '湖北', '湖南']
  } catch (error) {
    provinces.value = ['北京', '上海', '广东', '浙江', '江苏', '山东', '四川', '湖北', '湖南']
  }
})
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.main-content {
  padding: 40px 0;
}

.guide-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 40px;
}

.guide-step {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  background: var(--color-bg-card);
  border-radius: 25px;
  transition: all 0.3s;
  border: 1px solid var(--color-border);
}

.guide-step.active {
  background: linear-gradient(135deg, var(--color-accent-primary), var(--color-accent-secondary));
  color: #fff;
  border-color: var(--color-accent-primary);
}

.guide-step.completed {
  background: color-mix(in srgb, var(--color-accent-green) 8%, transparent);
  color: var(--color-accent-green);
  border-color: color-mix(in srgb, var(--color-accent-green) 30%, transparent);
}

.step-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-secondary);
}

.guide-step.active .step-icon,
.guide-step.completed .step-icon {
  background: rgba(255, 255, 255, 0.15);
  color: var(--color-accent-primary);
}

.guide-step.completed .step-icon {
  color: var(--color-accent-green);
}

.guide-arrow {
  font-size: 20px;
  color: var(--color-text-muted);
}

.form-section {
  padding: 40px;
}

.step-content h2 {
  font-size: 24px;
  margin-bottom: 10px;
}

.step-content p {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 30px;
}

.info-form {
  max-width: 800px;
}

.form-tip {
  display: block;
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 5px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
}

.preference-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preference-card {
  background: var(--color-bg-input);
  padding: 20px;
  border-radius: 10px;
  border: 1px solid var(--color-border);
}

.preference-card h3 {
  font-size: 16px;
  color: var(--color-text-primary);
  margin-bottom: 15px;
}

.preference-options {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.province-select, .major-select {
  width: 100%;
}

.preference-card label {
  display: block;
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 5px;
}

.result-section {
  margin-top: 20px;
}

.result-header {
  text-align: center;
  margin-bottom: 30px;
}

.result-header h2 {
  font-size: 28px;
  margin-bottom: 10px;
}

.result-header p {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.risk-summary {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.risk-card {
  flex: 2;
  padding: 25px;
  display: flex;
  align-items: center;
  gap: 20px;
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
  font-size: 40px;
}

.risk-info h3 {
  font-size: 20px;
  color: var(--color-text-primary);
  margin-bottom: 5px;
}

.risk-info p {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.stats-grid {
  flex: 1;
  display: flex;
  gap: 15px;
}

.stat-item {
  flex: 1;
  padding: 20px;
  text-align: center;
}

.stat-item .stat-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: var(--color-accent-primary);
}

.stat-item .stat-label {
  display: block;
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 5px;
}

.gradient-tabs {
  overflow: hidden;
}

.gradient-tabs :deep(.el-tabs--border-card) {
  background: transparent;
  border: none;
  box-shadow: none;
}

.gradient-tabs :deep(.el-tabs__header) {
  background: var(--color-bg-input);
  border-bottom: 1px solid var(--color-border);
}

.gradient-tabs :deep(.el-tabs__item) {
  color: var(--color-text-secondary);
}

.gradient-tabs :deep(.el-tabs__item.is-active) {
  color: var(--color-accent-primary);
}

.gradient-tabs :deep(.el-tabs__item:hover) {
  color: var(--color-accent-primary);
}

.gradient-tabs :deep(.el-tabs__content) {
  background: transparent;
}

.gradient-tabs :deep(.el-tabs--border-card > .el-tabs__header) {
  border-bottom: 1px solid var(--color-border);
}

.volunteer-list {
  padding: 20px;
}

.volunteer-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: var(--color-bg-input);
  border-radius: 10px;
  margin-bottom: 10px;
  border: 1px solid var(--color-border);
}

.item-rank {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-accent-primary), var(--color-accent-secondary));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}

.item-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.college-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.item-badges {
  display: flex;
  gap: 5px;
  flex-shrink: 0;
}

.item-detail {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 5px;
}

.item-detail span {
  margin-right: 15px;
}

.item-scores {
  font-size: 12px;
  color: var(--color-text-muted);
  overflow: hidden;
}

.item-scores span {
  margin-right: 15px;
  white-space: nowrap;
}

.item-probability {
  width: 120px;
  text-align: center;
}

.probability-bar {
  height: 8px;
  background: var(--color-bg-input);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 5px;
}

.probability-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-accent-primary), var(--color-accent-secondary));
  border-radius: 4px;
}

.probability-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-accent-primary);
}

.item-actions {
  width: 100px;
  text-align: right;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

@media (max-width: 992px) {
  .volunteer-item {
    flex-wrap: wrap;
  }

  .item-probability {
    width: 100%;
    text-align: left;
    margin-top: 10px;
  }

  .item-actions {
    width: 100%;
    text-align: right;
  }

  .risk-summary {
    flex-direction: column;
  }

  .guide-section {
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .guide-section {
    flex-wrap: wrap;
  }

  .risk-summary {
    flex-direction: column;
  }

  .stats-grid {
    flex-wrap: wrap;
  }

  .stat-item {
    flex: 1;
    min-width: 100px;
  }

  .volunteer-item {
    flex-wrap: wrap;
  }

  .item-probability {
    width: 100%;
    text-align: left;
  }

  .item-actions {
    width: 100%;
    text-align: left;
  }

  .form-section {
    padding: 20px 15px;
  }

  .preference-card {
    padding: 15px;
  }
}
</style>
