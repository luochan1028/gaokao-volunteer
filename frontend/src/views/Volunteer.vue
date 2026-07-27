<template>
  <div class="volunteer-page">
    <header class="header">
      <div class="container">
        <div class="logo">
          <h1>高考志愿填报平台</h1>
        </div>
        <nav class="nav">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/colleges" class="nav-link">院校查询</router-link>
          <router-link to="/recommend" class="nav-link">智能推荐</router-link>
          <router-link to="/volunteer" class="nav-link active">志愿模拟</router-link>
          <router-link to="/plans" class="nav-link">方案管理</router-link>
          <router-link to="/profile" class="nav-link">个人中心</router-link>
        </nav>
      </div>
    </header>

    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h2>志愿模拟</h2>
          <p>管理您的志愿列表，生成志愿填报方案</p>
        </div>

        <div class="add-section">
          <el-input
            v-model="searchText"
            placeholder="搜索院校或专业"
            class="search-input"
            @keyup.enter="searchItems"
          >
            <template #prefix><i class="el-icon-search"></i></template>
          </el-input>
          <el-button type="primary" @click="showAddModal = true">添加志愿</el-button>
        </div>

        <div class="volunteer-container">
          <div class="volunteer-left">
            <div class="volunteer-tabs">
              <el-tabs v-model="activeTab" type="border-card">
                <el-tab-pane label="🎯 冲" name="chong">
                  <div class="volunteer-list">
                    <div
                      v-for="(item, index) in chongList"
                      :key="item.id"
                      class="volunteer-item"
                    >
                      <div class="item-order">{{ (chongList.indexOf(item) + 1) + wenList.length + baoList.length }}</div>
                      <div class="item-content">
                        <div class="item-title">
                          <span>{{ item.collegeName }}</span>
                          <el-tag size="small" type="danger">冲</el-tag>
                        </div>
                        <div class="item-meta">
                          <span>{{ item.majorName }}</span>
                          <span>|</span>
                          <span>{{ item.collegeProvince }}</span>
                        </div>
                      </div>
                      <div class="item-prob">
                        <span>{{ (item.probability * 100).toFixed(0) }}%</span>
                      </div>
                      <div class="item-actions">
                        <el-button size="small" @click="moveUp(item)">↑</el-button>
                        <el-button size="small" @click="moveDown(item)">↓</el-button>
                        <el-button size="small" type="danger" @click="removeItem(item)">删除</el-button>
                      </div>
                    </div>
                    <div v-if="chongList.length === 0" class="empty-tip">
                      暂无"冲"志愿，请添加
                    </div>
                  </div>
                </el-tab-pane>
                
                <el-tab-pane label="✅ 稳" name="wen">
                  <div class="volunteer-list">
                    <div
                      v-for="(item, index) in wenList"
                      :key="item.id"
                      class="volunteer-item"
                    >
                      <div class="item-order">{{ (wenList.indexOf(item) + 1) + baoList.length }}</div>
                      <div class="item-content">
                        <div class="item-title">
                          <span>{{ item.collegeName }}</span>
                          <el-tag size="small" type="warning">稳</el-tag>
                        </div>
                        <div class="item-meta">
                          <span>{{ item.majorName }}</span>
                          <span>|</span>
                          <span>{{ item.collegeProvince }}</span>
                        </div>
                      </div>
                      <div class="item-prob">
                        <span>{{ (item.probability * 100).toFixed(0) }}%</span>
                      </div>
                      <div class="item-actions">
                        <el-button size="small" @click="moveUp(item)">↑</el-button>
                        <el-button size="small" @click="moveDown(item)">↓</el-button>
                        <el-button size="small" type="danger" @click="removeItem(item)">删除</el-button>
                      </div>
                    </div>
                    <div v-if="wenList.length === 0" class="empty-tip">
                      暂无"稳"志愿，请添加
                    </div>
                  </div>
                </el-tab-pane>
                
                <el-tab-pane label="🛡️ 保" name="bao">
                  <div class="volunteer-list">
                    <div
                      v-for="(item, index) in baoList"
                      :key="item.id"
                      class="volunteer-item"
                    >
                      <div class="item-order">{{ baoList.indexOf(item) + 1 }}</div>
                      <div class="item-content">
                        <div class="item-title">
                          <span>{{ item.collegeName }}</span>
                          <el-tag size="small" type="success">保</el-tag>
                        </div>
                        <div class="item-meta">
                          <span>{{ item.majorName }}</span>
                          <span>|</span>
                          <span>{{ item.collegeProvince }}</span>
                        </div>
                      </div>
                      <div class="item-prob">
                        <span>{{ (item.probability * 100).toFixed(0) }}%</span>
                      </div>
                      <div class="item-actions">
                        <el-button size="small" @click="moveUp(item)">↑</el-button>
                        <el-button size="small" @click="moveDown(item)">↓</el-button>
                        <el-button size="small" type="danger" @click="removeItem(item)">删除</el-button>
                      </div>
                    </div>
                    <div v-if="baoList.length === 0" class="empty-tip">
                      暂无"保"志愿，请添加
                    </div>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
          </div>

          <div class="volunteer-right">
            <div class="summary-card">
              <h3>志愿方案概览</h3>
              <div class="summary-stats">
                <div class="stat-row">
                  <span class="stat-label">总志愿数</span>
                  <span class="stat-value">{{ totalCount }}</span>
                </div>
                <div class="stat-row">
                  <span class="stat-label">冲</span>
                  <span class="stat-value chong">{{ chongList.length }}个</span>
                </div>
                <div class="stat-row">
                  <span class="stat-label">稳</span>
                  <span class="stat-value wen">{{ wenList.length }}个</span>
                </div>
                <div class="stat-row">
                  <span class="stat-label">保</span>
                  <span class="stat-value bao">{{ baoList.length }}个</span>
                </div>
              </div>
              <div class="risk-assessment">
                <h4>风险评估</h4>
                <div class="risk-bar">
                  <div class="risk-fill" :style="{ width: riskLevel.width }" :class="riskLevel.class"></div>
                </div>
                <div class="risk-info">
                  <span class="risk-label">{{ riskLevel.label }}</span>
                  <span class="risk-text">{{ riskLevel.text }}</span>
                </div>
                <div class="risk-details">
                  <div class="detail-item">
                    <span>滑档概率</span>
                    <span>{{ slipProbability }}%</span>
                  </div>
                  <div class="detail-item">
                    <span>浪费指数</span>
                    <span>{{ wasteScoreIndex }}%</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="action-card">
              <el-button type="primary" @click="saveCurrentPlan" class="btn-full">保存方案</el-button>
              <el-button type="success" @click="generateReport" class="btn-full">生成报告</el-button>
              <el-button @click="clearAll" class="btn-full">清空列表</el-button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <el-dialog v-model="showAddModal" title="添加志愿" width="700px">
      <div class="add-modal-content">
        <el-input
          v-model="modalSearch"
          placeholder="搜索院校"
          class="modal-search"
          @keyup.enter="searchCollegesForAdd"
        >
          <template #prefix><i class="el-icon-search"></i></template>
        </el-input>
        
        <div class="college-list">
          <div
            v-for="college in searchColleges"
            :key="college.id"
            class="college-item"
            @click="selectCollege(college)"
          >
            <div class="college-info">
              <div class="college-header">
                <span>{{ college.name }}</span>
                <div class="college-badges">
                  <el-tag v-if="college.is985" type="danger" size="small">985</el-tag>
                  <el-tag v-if="college.is211" type="warning" size="small">211</el-tag>
                  <el-tag v-if="college.isDoubleFirstClass" type="primary" size="small">双一流</el-tag>
                </div>
              </div>
              <p>{{ college.province }}·{{ college.city }} | {{ getCollegeTypeLabel(college.type) }}</p>
            </div>
          </div>
        </div>

        <div v-if="selectedCollege" class="major-section">
          <h4>选择专业</h4>
          <el-checkbox-group v-model="selectedMajors">
            <el-checkbox
              v-for="major in collegeMajors"
              :key="major.id"
              :label="major.id"
            >
              {{ major.name }}
            </el-checkbox>
          </el-checkbox-group>
        </div>

        <div class="gradient-section">
          <h4>志愿梯度</h4>
          <el-radio-group v-model="selectedGradient">
            <el-radio label="CHONG">🎯 冲（高风险）</el-radio>
            <el-radio label="WEN">✅ 稳（中风险）</el-radio>
            <el-radio label="BAO">🛡️ 保（低风险）</el-radio>
          </el-radio-group>
        </div>
      </div>
      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="confirmAdd">确定添加</el-button>
      </template>
    </el-dialog>

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
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { searchCollegesApi, getMajorsByCollegeApi } from '@/api/college'
import { getUserVolunteerListApi, removeFromVolunteerListApi, addToVolunteerListApi, savePlanApi } from '@/api/volunteer'

const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref('chong')
const searchText = ref('')
const showAddModal = ref(false)
const modalSearch = ref('')
const searchColleges = ref([])
const selectedCollege = ref(null)
const collegeMajors = ref([])
const selectedMajors = ref([])
const selectedGradient = ref('CHONG')

const volunteerItems = ref([])

const chongList = computed(() => volunteerItems.value.filter(item => item.gradientType === 'CHONG'))
const wenList = computed(() => volunteerItems.value.filter(item => item.gradientType === 'WEN'))
const baoList = computed(() => volunteerItems.value.filter(item => item.gradientType === 'BAO'))

const totalCount = computed(() => volunteerItems.value.length)

const slipProbability = computed(() => {
  if (baoList.value.length === 0) return 50
  const avgProb = baoList.value.reduce((sum, item) => sum + (item.probability || 0), 0) / baoList.value.length
  return ((1 - avgProb) * 100).toFixed(1)
})

const wasteScoreIndex = computed(() => {
  return '0.0'
})

const riskLevel = computed(() => {
  const slip = parseFloat(slipProbability.value)
  if (slip < 10) return { label: '安全', text: '志愿梯度合理', width: '25%', class: 'risk-safe' }
  if (slip < 25) return { label: '较安全', text: '建议增加保底数量', width: '50%', class: 'risk-medium' }
  if (slip < 40) return { label: '中等风险', text: '滑档风险较高', width: '75%', class: 'risk-medium' }
  return { label: '高风险', text: '滑档风险极高', width: '100%', class: 'risk-high' }
})

const getCollegeTypeLabel = (type) => {
  const types = {
    'PUBLIC': '公办',
    'PRIVATE': '民办',
    'INDEPENDENT': '独立学院',
    'MILITARY': '军事院校'
  }
  return types[type] || type
}

const loadVolunteerList = async () => {
  try {
    const response = await getUserVolunteerListApi()
    volunteerItems.value = response.data.data || []
  } catch (error) {
    console.error('加载志愿列表失败:', error)
  }
}

const searchCollegesForAdd = async () => {
  try {
    const response = await searchCollegesApi({ name: modalSearch.value, size: 10 })
    searchColleges.value = response.data.data.content || []
  } catch (error) {
    console.error('搜索院校失败:', error)
  }
}

const selectCollege = async (college) => {
  selectedCollege.value = college
  try {
    const response = await getMajorsByCollegeApi(college.id)
    collegeMajors.value = response.data.data || []
  } catch (error) {
    console.error('获取专业列表失败:', error)
  }
}

const confirmAdd = async () => {
  if (!selectedCollege.value) {
    alert('请先选择院校')
    return
  }
  
  for (const majorId of selectedMajors.value) {
    const major = collegeMajors.value.find(m => m.id === majorId)
    try {
      await addToVolunteerListApi(selectedCollege.value.id, majorId, major.name)
    } catch (error) {
      console.error('添加志愿失败:', error)
    }
  }
  
  showAddModal.value = false
  selectedCollege.value = null
  selectedMajors.value = []
  await loadVolunteerList()
  alert('添加成功')
}

const removeItem = async (item) => {
  try {
    await removeFromVolunteerListApi(item.id)
    await loadVolunteerList()
  } catch (error) {
    console.error('删除志愿失败:', error)
  }
}

const moveUp = (item) => {
  const list = item.gradientType === 'CHONG' ? chongList.value :
               item.gradientType === 'WEN' ? wenList.value : baoList.value
  const index = list.indexOf(item)
  if (index > 0) {
    const temp = volunteerItems.value[volunteerItems.value.indexOf(item)]
    const prevIndex = volunteerItems.value.indexOf(list[index - 1])
    volunteerItems.value[volunteerItems.value.indexOf(item)] = volunteerItems.value[prevIndex]
    volunteerItems.value[prevIndex] = temp
  }
}

const moveDown = (item) => {
  const list = item.gradientType === 'CHONG' ? chongList.value :
               item.gradientType === 'WEN' ? wenList.value : baoList.value
  const index = list.indexOf(item)
  if (index < list.length - 1) {
    const temp = volunteerItems.value[volunteerItems.value.indexOf(item)]
    const nextIndex = volunteerItems.value.indexOf(list[index + 1])
    volunteerItems.value[volunteerItems.value.indexOf(item)] = volunteerItems.value[nextIndex]
    volunteerItems.value[nextIndex] = temp
  }
}

const saveCurrentPlan = async () => {
  const planName = prompt('请输入方案名称：', '我的志愿方案')
  if (!planName) return
  
  try {
    const response = await savePlanApi({
      name: planName,
      description: '手动创建的志愿方案',
      items: volunteerItems.value
    })
    alert('保存成功')
    router.push('/plans')
  } catch (error) {
    console.error('保存方案失败:', error)
    alert('保存失败')
  }
}

const generateReport = () => {
  alert('报告生成功能开发中')
}

const clearAll = () => {
  if (confirm('确定要清空所有志愿吗？')) {
    volunteerItems.value = []
  }
}

onMounted(() => {
  loadVolunteerList()
})
</script>

<style scoped>
.volunteer-page {
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

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 14px;
  color: #666;
}

.add-section {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

.search-input {
  flex: 1;
  max-width: 400px;
}

.volunteer-container {
  display: flex;
  gap: 30px;
}

.volunteer-left {
  flex: 3;
}

.volunteer-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.volunteer-tabs {
  background: white;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.volunteer-list {
  padding: 15px;
}

.volunteer-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 10px;
}

.item-order {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #667eea;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 13px;
}

.item-content {
  flex: 1;
}

.item-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.item-meta {
  font-size: 13px;
  color: #666;
}

.item-meta span {
  margin-right: 8px;
}

.item-prob {
  width: 70px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #667eea;
}

.item-actions {
  display: flex;
  gap: 5px;
}

.empty-tip {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 14px;
}

.summary-card {
  background: white;
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.summary-card h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
}

.summary-stats {
  margin-bottom: 25px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.stat-value.chong {
  color: #f44336;
}

.stat-value.wen {
  color: #ff9800;
}

.stat-value.bao {
  color: #4caf50;
}

.risk-assessment h4 {
  font-size: 15px;
  color: #333;
  margin-bottom: 15px;
}

.risk-bar {
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 10px;
}

.risk-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s;
}

.risk-safe {
  background: #4caf50;
}

.risk-medium {
  background: #ff9800;
}

.risk-high {
  background: #f44336;
}

.risk-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.risk-label {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.risk-text {
  font-size: 13px;
  color: #666;
}

.risk-details {
  display: flex;
  justify-content: space-between;
}

.detail-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
  flex: 1;
  margin: 0 5px;
}

.detail-item span:first-child {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.detail-item span:last-child {
  font-size: 16px;
  font-weight: 600;
  color: #667eea;
}

.action-card {
  background: white;
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.btn-full {
  width: 100%;
  margin-bottom: 10px;
}

.add-modal-content {
  max-height: 500px;
  overflow-y: auto;
}

.modal-search {
  margin-bottom: 20px;
}

.college-list {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 20px;
}

.college-item {
  padding: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.college-item:hover {
  border-color: #667eea;
  background: #f5f7fa;
}

.college-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.college-header span {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.college-badges {
  display: flex;
  gap: 5px;
}

.college-item p {
  font-size: 13px;
  color: #666;
  margin: 0;
}

.major-section, .gradient-section {
  margin-bottom: 20px;
}

.major-section h4, .gradient-section h4 {
  font-size: 14px;
  color: #333;
  margin-bottom: 15px;
}

.major-section .el-checkbox {
  margin-right: 15px;
  margin-bottom: 10px;
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
  .volunteer-container {
    flex-direction: column;
  }
  
  .volunteer-item {
    flex-wrap: wrap;
  }
  
  .item-actions {
    width: 100%;
    justify-content: flex-end;
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