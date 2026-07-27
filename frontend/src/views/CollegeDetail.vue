
<template>
  <div class="detail-page">
    <header class="header">
      <div class="container">
        <div class="logo">
          <h1>高考志愿填报平台</h1>
        </div>
        <nav class="nav">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/colleges" class="nav-link">院校查询</router-link>
          <router-link to="/majors" class="nav-link">专业查询</router-link>
          <router-link to="/recommend" class="nav-link">智能推荐</router-link>
          <router-link v-if="authStore.isLoggedIn" to="/profile" class="nav-link">个人中心</router-link>
        </nav>
      </div>
    </header>

    <main class="main-content" v-if="college">
      <div class="container">
        <div class="back-link">
          <router-link to="/colleges">← 返回院校列表</router-link>
        </div>

        <div class="college-header">
          <div class="college-badges">
            <span v-if="college.is985" class="badge badge-985">985</span>
            <span v-if="college.is211" class="badge badge-211">211</span>
            <span v-if="college.isDoubleFirstClass" class="badge badge-double">双一流</span>
          </div>
          <h2>{{ college.name }}</h2>
          <p>{{ college.province }} · {{ college.city }} · {{ getTypeName(college.type) }}</p>
        </div>

        <div class="info-section">
          <el-row :gutter="20">
            <el-col :span="16">
              <div class="info-card">
                <h3>院校简介</h3>
                <p>{{ college.description || '暂无简介' }}</p>
              </div>

              <div class="info-card">
                <h3>院校特色</h3>
                <p>{{ college.features || '暂无特色信息' }}</p>
              </div>

              <div class="info-card">
                <h3>优势学科</h3>
                <p>{{ college.advantages || '暂无优势学科信息' }}</p>
              </div>
            </el-col>

            <el-col :span="8">
              <div class="stats-card">
                <h3>院校数据</h3>
                <div class="stat-item">
                  <span class="stat-label">满意度</span>
                  <span class="stat-value">{{ college.satisfactionScore || '-' }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">在校生人数</span>
                  <span class="stat-value">{{ college.studentCount || '-' }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">教职工人数</span>
                  <span class="stat-value">{{ college.teacherCount || '-' }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">创办时间</span>
                  <span class="stat-value">{{ college.foundedYear || '-' }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">研究生招生</span>
                  <span class="stat-value">{{ college.hasGraduateProgram ? '有' : '无' }}</span>
                </div>
              </div>

              <div class="actions-card">
                <el-button type="primary" @click="addToVolunteer" class="action-btn">加入志愿列表</el-button>
                <a :href="college.website" target="_blank" class="website-link">访问官网</a>
              </div>
            </el-col>
          </el-row>
        </div>

        <div class="tabs-section">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="开设专业" name="majors">
              <div class="majors-list">
                <div v-for="major in majors" :key="major.id" class="major-item">
                  <div class="major-info">
                    <h4>{{ major.name }}</h4>
                    <p>{{ getCategoryName(major.category) }} · 满意度: {{ major.satisfactionScore }}</p>
                  </div>
                  <div class="major-salary">
                    平均薪资: {{ major.averageSalary ? major.averageSalary + '元' : '-' }}
                  </div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="历年分数线" name="scores">
              <div class="scores-list">
                <el-table :data="scores" border>
                  <el-table-column prop="year" label="年份" />
                  <el-table-column prop="majorName" label="专业" />
                  <el-table-column prop="minScore" label="最低分" />
                  <el-table-column prop="maxScore" label="最高分" />
                  <el-table-column prop="averageScore" label="平均分" />
                  <el-table-column prop="minRank" label="最低位次" />
                  <el-table-column prop="maxRank" label="最高位次" />
                </el-table>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { getCollegeByIdApi, getMajorsByCollegeApi, getEnrollmentScoresApi } from '@/api/college'
import { addToVolunteerListApi } from '@/api/volunteer'

const route = useRoute()
const authStore = useAuthStore()

const college = ref(null)
const majors = ref([])
const scores = ref([])
const activeTab = ref('majors')

onMounted(() => {
  loadCollege()
})

async function loadCollege() {
  const id = route.params.id
  try {
    const response = await getCollegeByIdApi(id)
    college.value = response.data.data
    loadMajors(id)
    loadScores(id)
  } catch (error) {
    console.error('加载院校详情失败', error)
  }
}

async function loadMajors(id) {
  try {
    const response = await getMajorsByCollegeApi(id)
    majors.value = response.data.data
  } catch (error) {
    console.error('加载专业列表失败', error)
  }
}

async function loadScores(id) {
  try {
    const response = await getEnrollmentScoresApi(id, '2024')
    scores.value = response.data.data
  } catch (error) {
    console.error('加载分数线失败', error)
  }
}

async function addToVolunteer() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    await addToVolunteerListApi(college.value.id, null, college.value.name)
    ElMessage.success('已加入志愿列表')
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

function getTypeName(type) {
  const types = {
    PUBLIC: '公办',
    PRIVATE: '民办',
    INDEPENDENT: '独立学院',
    VOCATIONAL: '职业院校'
  }
  return types[type] || type
}

function getCategoryName(category) {
  const categories = {
    PHILOSOPHY: '哲学',
    ECONOMICS: '经济学',
    LAW: '法学',
    EDUCATION: '教育学',
    LITERATURE: '文学',
    HISTORY: '历史学',
    SCIENCE: '理学',
    ENGINEERING: '工学',
    AGRICULTURE: '农学',
    MEDICINE: '医学',
    MANAGEMENT: '管理学',
    ART: '艺术学'
  }
  return categories[category] || category
}
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f8f9fa;
}

.header {
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
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
  font-size: 20px;
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

.nav-link:hover {
  background: #667eea;
  color: white;
}

.main-content {
  padding: 30px 0;
}

.back-link {
  margin-bottom: 20px;
}

.back-link a {
  color: #667eea;
  text-decoration: none;
}

.college-header {
  background: white;
  padding: 30px;
  border-radius: 15px;
  margin-bottom: 20px;
}

.college-badges {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
}

.badge {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 15px;
}

.badge-985 {
  background: #dc3545;
  color: white;
}

.badge-211 {
  background: #fd7e14;
  color: white;
}

.badge-double {
  background: #667eea;
  color: white;
}

.college-header h2 {
  font-size: 32px;
  margin: 0 0 10px;
}

.college-header p {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.info-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.info-card h3 {
  font-size: 18px;
  margin: 0 0 15px;
  color: #333;
}

.info-card p {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  margin: 0;
}

.stats-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.stats-card h3 {
  font-size: 18px;
  margin: 0 0 20px;
  color: #333;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-value {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.actions-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 25px;
  border-radius: 12px;
  text-align: center;
}

.action-btn {
  width: 100%;
  margin-bottom: 15px;
  border-radius: 20px;
}

.website-link {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  font-size: 14px;
}

.website-link:hover {
  text-decoration: underline;
}

.tabs-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
  margin-top: 20px;
}

.majors-list {
  margin-top: 20px;
}

.major-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.major-item:last-child {
  border-bottom: none;
}

.major-info h4 {
  font-size: 16px;
  margin: 0 0 5px;
}

.major-info p {
  font-size: 13px;
  color: #666;
  margin: 0;
}

.major-salary {
  font-size: 14px;
  color: #667eea;
  font-weight: 600;
}

.scores-list {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .college-header h2 {
    font-size: 24px;
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
