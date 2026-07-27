<template>
  <div class="majors-page">
    <header class="header">
      <div class="container">
        <div class="logo">
          <h1>高考志愿填报平台</h1>
        </div>
        <nav class="nav">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/colleges" class="nav-link">院校查询</router-link>
          <router-link to="/majors" class="nav-link active">专业查询</router-link>
          <router-link to="/recommend" class="nav-link">智能推荐</router-link>
          <router-link v-if="!authStore.isLoggedIn" to="/login" class="nav-link btn-login">登录</router-link>
          <router-link v-if="authStore.isLoggedIn" to="/profile" class="nav-link">个人中心</router-link>
        </nav>
      </div>
    </header>

    <main class="main-content">
      <div class="container">
        <div class="search-section">
          <h2>专业查询</h2>
          <p>覆盖全国2000+专业，了解专业详情与就业前景</p>
          <div class="search-form">
            <el-input
              v-model="searchForm.name"
              placeholder="输入专业名称"
              class="search-input"
              @keyup.enter="searchMajors"
            >
              <template #prefix><i class="el-icon-search"></i></template>
            </el-input>
            <el-select
              v-model="searchForm.category"
              placeholder="专业类别"
              class="search-select"
            >
              <el-option label="全部" value="" />
              <el-option label="哲学" value="PHILOSOPHY" />
              <el-option label="经济学" value="ECONOMICS" />
              <el-option label="法学" value="LAW" />
              <el-option label="教育学" value="EDUCATION" />
              <el-option label="文学" value="LITERATURE" />
              <el-option label="历史学" value="HISTORY" />
              <el-option label="理学" value="SCIENCE" />
              <el-option label="工学" value="ENGINEERING" />
              <el-option label="农学" value="AGRICULTURE" />
              <el-option label="医学" value="MEDICINE" />
              <el-option label="管理学" value="MANAGEMENT" />
              <el-option label="艺术学" value="ART" />
            </el-select>
            <el-select
              v-model="searchForm.isHot"
              placeholder="热门专业"
              class="search-select"
            >
              <el-option label="全部" value="" />
              <el-option label="热门" :value="true" />
              <el-option label="非热门" :value="false" />
            </el-select>
            <el-button type="primary" @click="searchMajors">搜索</el-button>
          </div>
        </div>

        <div class="category-filter">
          <el-tag
            v-for="cat in categories"
            :key="cat.value"
            :type="searchForm.category === cat.value ? 'primary' : 'info'"
            :closable="false"
            @click="searchForm.category = cat.value; searchMajors()"
          >
            {{ cat.label }}
          </el-tag>
        </div>

        <div class="result-section">
          <div class="result-header">
            <span>共找到 {{ total }} 个专业</span>
            <el-select v-model="sortField" class="sort-select" @change="searchMajors">
              <el-option label="默认排序" value="" />
              <el-option label="满意度最高" value="satisfactionScore" />
              <el-option label="就业率最高" value="employmentRate" />
              <el-option label="平均薪资最高" value="averageSalary" />
            </el-select>
          </div>

          <div class="major-grid">
            <div
              v-for="major in majors"
              :key="major.id"
              class="major-card"
              @click="showMajorDetail(major)"
            >
              <div class="major-header">
                <h3>{{ major.name }}</h3>
                <div class="major-badges">
                  <el-tag v-if="major.isHot" type="danger" size="small">热门</el-tag>
                  <el-tag v-if="major.isNationalKey" type="primary" size="small">国家级重点</el-tag>
                </div>
              </div>
              <div class="major-info">
                <p class="category">{{ getCategoryLabel(major.category) }}</p>
                <p class="description">{{ truncate(major.description, 80) }}</p>
              </div>
              <div class="major-stats">
                <div class="stat-item">
                  <span class="stat-label">满意度</span>
                  <span class="stat-value">{{ major.satisfactionScore || '-' }}%</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">就业率</span>
                  <span class="stat-value">{{ major.employmentRate || '-' }}%</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">平均薪资</span>
                  <span class="stat-value">¥{{ formatSalary(major.averageSalary) }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              @current-change="handlePageChange"
              layout="total, prev, pager, next, jumper"
            />
          </div>
        </div>
      </div>
    </main>

    <el-dialog v-model="detailVisible" title="专业详情" width="800px">
      <div v-if="selectedMajor" class="major-detail">
        <div class="detail-header">
          <h2>{{ selectedMajor.name }}</h2>
          <div class="detail-badges">
            <el-tag v-if="selectedMajor.isHot" type="danger">热门专业</el-tag>
            <el-tag v-if="selectedMajor.isNationalKey" type="primary">国家级重点专业</el-tag>
          </div>
        </div>
        <div class="detail-content">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="detail-section">
                <h3>专业类别</h3>
                <p>{{ getCategoryLabel(selectedMajor.category) }}</p>
              </div>
              <div class="detail-section">
                <h3>专业代码</h3>
                <p>{{ selectedMajor.code || '-' }}</p>
              </div>
              <div class="detail-section">
                <h3>选科要求</h3>
                <p>{{ selectedMajor.subjectRequirements || '无特殊要求' }}</p>
              </div>
              <div class="detail-section">
                <h3>核心课程</h3>
                <p>{{ selectedMajor.coreCourses || '-' }}</p>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-section">
                <h3>专业简介</h3>
                <p>{{ selectedMajor.description || '-' }}</p>
              </div>
              <div class="detail-section">
                <h3>就业方向</h3>
                <p>{{ selectedMajor.employmentDirections || '-' }}</p>
              </div>
              <div class="detail-section">
                <h3>职业前景</h3>
                <p>{{ selectedMajor.careerProspects || '-' }}</p>
              </div>
              <div class="detail-section">
                <h3>相关职业</h3>
                <p>{{ selectedMajor.relatedOccupations || '-' }}</p>
              </div>
            </el-col>
          </el-row>
          <div class="detail-stats">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="stat-card">
                  <div class="stat-icon">😊</div>
                  <div class="stat-info">
                    <span class="stat-value">{{ selectedMajor.satisfactionScore || 0 }}%</span>
                    <span class="stat-label">满意度</span>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-card">
                  <div class="stat-icon">💼</div>
                  <div class="stat-info">
                    <span class="stat-value">{{ selectedMajor.employmentRate || 0 }}%</span>
                    <span class="stat-label">就业率</span>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-card">
                  <div class="stat-icon">💰</div>
                  <div class="stat-info">
                    <span class="stat-value">¥{{ formatSalary(selectedMajor.averageSalary) }}</span>
                    <span class="stat-label">平均薪资</span>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
      </div>
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
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/store/auth'
import { searchMajorsApi } from '@/api/college'

const authStore = useAuthStore()

const searchForm = reactive({
  name: '',
  category: '',
  isHot: ''
})

const majors = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const sortField = ref('')

const detailVisible = ref(false)
const selectedMajor = ref(null)

const categories = [
  { label: '全部', value: '' },
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

const getCategoryLabel = (category) => {
  const cat = categories.find(c => c.value === category)
  return cat ? cat.label : category
}

const truncate = (text, length) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

const formatSalary = (salary) => {
  if (!salary) return '-'
  return salary.toLocaleString()
}

const searchMajors = async () => {
  try {
    const params = {
      name: searchForm.name,
      category: searchForm.category || undefined,
      isHot: searchForm.isHot === '' ? undefined : searchForm.isHot,
      page: currentPage.value - 1,
      size: pageSize.value
    }
    const response = await searchMajorsApi(params)
    majors.value = response.data.data.content
    total.value = response.data.data.totalElements
  } catch (error) {
    console.error('搜索专业失败:', error)
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  searchMajors()
}

const showMajorDetail = (major) => {
  selectedMajor.value = major
  detailVisible.value = true
}

onMounted(() => {
  searchMajors()
})
</script>

<style scoped>
.majors-page {
  min-height: 100vh;
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

.btn-login {
  background: #667eea;
  color: white !important;
}

.btn-login:hover {
  background: #5a6fd6 !important;
}

.main-content {
  padding: 40px 0;
}

.search-section {
  text-align: center;
  margin-bottom: 40px;
}

.search-section h2 {
  font-size: 32px;
  color: #333;
  margin-bottom: 10px;
}

.search-section p {
  font-size: 16px;
  color: #666;
  margin-bottom: 30px;
}

.search-form {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
}

.search-input {
  width: 300px;
}

.search-select {
  width: 150px;
}

.category-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 30px;
  justify-content: center;
}

.result-section {
  background: white;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.result-header span {
  color: #666;
  font-size: 14px;
}

.sort-select {
  width: 150px;
}

.major-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.major-card {
  background: #f8f9fa;
  padding: 25px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.major-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.major-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.major-header h3 {
  font-size: 18px;
  color: #333;
  margin: 0;
}

.major-badges {
  display: flex;
  gap: 5px;
}

.major-info .category {
  color: #667eea;
  font-size: 13px;
  margin-bottom: 8px;
}

.major-info .description {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}

.major-stats {
  display: flex;
  gap: 20px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e0e0e0;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.major-detail {
  padding: 10px 0;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-header h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.detail-badges {
  display: flex;
  gap: 10px;
}

.detail-content {
  margin-bottom: 20px;
}

.detail-section {
  margin-bottom: 15px;
}

.detail-section h3 {
  font-size: 14px;
  color: #999;
  margin-bottom: 5px;
}

.detail-section p {
  font-size: 15px;
  color: #333;
  line-height: 1.6;
}

.detail-stats {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 15px;
  background: #f8f9fa;
  padding: 15px;
  border-radius: 10px;
}

.stat-icon {
  font-size: 32px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-info .stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #333;
}

.stat-info .stat-label {
  font-size: 12px;
  color: #999;
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
  .major-grid {
    grid-template-columns: repeat(1, 1fr);
  }
  
  .search-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input, .search-select {
    width: 100%;
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