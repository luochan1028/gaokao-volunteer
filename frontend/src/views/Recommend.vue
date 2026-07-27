<template>
  <div class="recommend-page">
    <header class="header">
      <div class="container">
        <div class="logo">
          <h1>高考志愿填报平台</h1>
        </div>
        <nav class="nav">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/colleges" class="nav-link">院校查询</router-link>
          <router-link to="/majors" class="nav-link">专业查询</router-link>
          <router-link to="/recommend" class="nav-link active">智能推荐</router-link>
          <router-link to="/volunteer" class="nav-link">志愿模拟</router-link>
          <router-link to="/profile" class="nav-link">个人中心</router-link>
        </nav>
      </div>
    </header>

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

        <div class="form-section" v-if="currentStep <= 2">
          <div v-if="currentStep === 1" class="step-content">
            <h2>第一步：填写基本信息</h2>
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
            <h2>第二步：设置筛选偏好</h2>
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
            <h2>智能推荐结果</h2>
            <p>基于您的位次和偏好，系统为您生成以下志愿方案</p>
          </div>

          <div class="risk-summary">
            <div class="risk-card" :class="getRiskClass(recommendationResult.riskAssessment.riskLevel)">
              <div class="risk-icon">{{ getRiskIcon(recommendationResult.riskAssessment.riskLevel) }}</div>
              <div class="risk-info">
                <h3>{{ recommendationResult.riskAssessment.riskLevel }}</h3>
                <p>{{ recommendationResult.riskAssessment.suggestions }}</p>
              </div>
            </div>
            
            <div class="stats-grid">
              <div class="stat-item">
                <span class="stat-value">{{ (recommendationResult.riskAssessment.slipProbability * 100).toFixed(1) }}%</span>
                <span class="stat-label">滑档概率</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ (recommendationResult.riskAssessment.wasteScoreIndex * 100).toFixed(1) }}%</span>
                <span class="stat-label">浪费分数指数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ recommendationResult.riskAssessment.totalRecommended }}</span>
                <span class="stat-label">推荐总数</span>
              </div>
            </div>
          </div>

          <div class="gradient-tabs">
            <el-tabs v-model="activeTab" type="border-card">
              <el-tab-pane label="🎯 冲（{{ recommendationResult.chongList.length }}所）" name="chong">
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
                        <span>去年最低分：{{ item.lastYearMinScore }}</span>
                        <span>最低位次：{{ item.lastYearMinRank }}</span>
                      </div>
                    </div>
                    <div class="item-probability">
                      <div class="probability-bar">
                        <div class="probability-fill" :style="{ width: (item.probability * 100) + '%' }"></div>
                      </div>
                      <span class="probability-text">{{ (item.probability * 100).toFixed(0) }}%</span>
                      <el-tag size="small" type="danger">高风险</el-tag>
                    </div>
                    <div class="item-actions">
                      <el-button size="small" @click="addToVolunteer(item)">加入志愿</el-button>
                    </div>
                  </div>
                </div>
              </el-tab-pane>
              
              <el-tab-pane label="✅ 稳（{{ recommendationResult.wenList.length }}所）" name="wen">
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
                        <span>去年最低分：{{ item.lastYearMinScore }}</span>
                        <span>最低位次：{{ item.lastYearMinRank }}</span>
                      </div>
                    </div>
                    <div class="item-probability">
                      <div class="probability-bar">
                        <div class="probability-fill" :style="{ width: (item.probability * 100) + '%' }"></div>
                      </div>
                      <span class="probability-text">{{ (item.probability * 100).toFixed(0) }}%</span>
                      <el-tag size="small" type="warning">中风险</el-tag>
                    </div>
                    <div class="item-actions">
                      <el-button size="small" @click="addToVolunteer(item)">加入志愿</el-button>
                    </div>
                  </div>
                </div>
              </el-tab-pane>
              
              <el-tab-pane label="🛡️ 保（{{ recommendationResult.baoList.length }}所）" name="bao">
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
                        <span>去年最低分：{{ item.lastYearMinScore }}</span>
                        <span>最低位次：{{ item.lastYearMinRank }}</span>
                      </div>
                    </div>
                    <div class="item-probability">
                      <div class="probability-bar">
                        <div class="probability-fill" :style="{ width: (item.probability * 100) + '%' }"></div>
                      </div>
                      <span class="probability-text">{{ (item.probability * 100).toFixed(0) }}%</span>
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

    <footer class="footer">
      <div class="container">
        <p>&copy; 2024 高考志愿填报平台. All rights reserved.</p>
        <p>系统推荐结果仅作为参考，请以各省官方志愿系统为准</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/store/auth'
import { getAllProvincesApi, getAllYearsApi } from '@/api/college'
import { generateRecommendationsApi } from '@/api/recommend'
import { addToVolunteerListApi } from '@/api/volunteer'

const authStore = useAuthStore()

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
.recommend-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
}

.logo h1 {
  font-size: 24px;
  color: #667eea;
  margin: 0;
}

.nav {
  display: flex;
  gap: 20px;
  align-items: center;
}

.nav-link {
  text-decoration: none;
  color: #333;
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.3s;
}

.nav-link:hover, .nav-link.active {
  background: #667eea;
  color: white;
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
  background: white;
  border-radius: 25px;
  transition: all 0.3s;
}

.guide-step.active {
  background: #667eea;
  color: white;
}

.guide-step.completed {
  background: #e8f5e9;
  color: #4caf50;
}

.step-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.guide-step.active .step-icon,
.guide-step.completed .step-icon {
  background: white;
  color: #667eea;
}

.guide-step.completed .step-icon {
  color: #4caf50;
}

.guide-arrow {
  font-size: 20px;
  color: #ccc;
}

.form-section {
  background: white;
  border-radius: 15px;
  padding: 40px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.step-content h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.step-content p {
  font-size: 14px;
  color: #666;
  margin-bottom: 30px;
}

.info-form {
  max-width: 800px;
}

.form-tip {
  display: block;
  font-size: 12px;
  color: #999;
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
  background: #f8f9fa;
  padding: 20px;
  border-radius: 10px;
}

.preference-card h3 {
  font-size: 16px;
  color: #333;
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
  color: #666;
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
  color: #333;
  margin-bottom: 10px;
}

.result-header p {
  font-size: 14px;
  color: #666;
}

.risk-summary {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.risk-card {
  flex: 2;
  padding: 25px;
  border-radius: 15px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.risk-safe {
  background: #e8f5e9;
  border-left: 5px solid #4caf50;
}

.risk-medium {
  background: #fff3e0;
  border-left: 5px solid #ff9800;
}

.risk-high {
  background: #ffebee;
  border-left: 5px solid #f44336;
}

.risk-icon {
  font-size: 40px;
}

.risk-info h3 {
  font-size: 20px;
  color: #333;
  margin-bottom: 5px;
}

.risk-info p {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.stats-grid {
  flex: 1;
  display: flex;
  gap: 15px;
}

.stat-item {
  flex: 1;
  background: white;
  padding: 20px;
  border-radius: 10px;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.stat-item .stat-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
}

.stat-item .stat-label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.gradient-tabs {
  background: white;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.volunteer-list {
  padding: 20px;
}

.volunteer-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 10px;
  margin-bottom: 10px;
}

.item-rank {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #667eea;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}

.item-info {
  flex: 1;
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
  color: #333;
}

.item-badges {
  display: flex;
  gap: 5px;
}

.item-detail {
  font-size: 13px;
  color: #666;
  margin-bottom: 5px;
}

.item-detail span {
  margin-right: 15px;
}

.item-scores {
  font-size: 12px;
  color: #999;
}

.item-scores span {
  margin-right: 15px;
}

.item-probability {
  width: 120px;
  text-align: center;
}

.probability-bar {
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 5px;
}

.probability-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 4px;
}

.probability-text {
  font-size: 14px;
  font-weight: 600;
  color: #667eea;
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

.footer {
  background: #333;
  color: white;
  padding: 30px 0;
  text-align: center;
  margin-top: 40px;
}

.footer p {
  margin: 5px 0;
  font-size: 14px;
  opacity: 0.8;
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
  
  .header .container {
    flex-direction: column;
    gap: 10px;
  }
  
  .nav {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>