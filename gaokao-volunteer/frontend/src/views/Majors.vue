<template>
  <TechLayout>
    <div class="majors-page">
      <main class="main-content">
        <div class="container">
          <div class="search-section">
            <h2 class="heading-tech">专业查询</h2>
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

          <div class="result-section glass-card tech-border">
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
                    <span class="stat-value">{{ major.satisfactionScore != null ? Number(major.satisfactionScore).toFixed(2) : '-' }}%</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">就业率</span>
                    <span class="stat-value">{{ major.employmentRate != null ? Number(major.employmentRate).toFixed(2) : '-' }}%</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">平均薪资</span>
                    <span class="stat-value">¥{{ major.averageSalary != null ? Number(major.averageSalary).toFixed(2) : '-' }}</span>
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
            <h2 class="heading-tech">{{ selectedMajor.name }}</h2>
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
                      <span class="stat-value">{{ Number(selectedMajor.satisfactionScore || 0).toFixed(2) }}%</span>
                      <span class="stat-label">满意度</span>
                    </div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-card">
                    <div class="stat-icon">💼</div>
                    <div class="stat-info">
                      <span class="stat-value">{{ Number(selectedMajor.employmentRate || 0).toFixed(2) }}%</span>
                      <span class="stat-label">就业率</span>
                    </div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-card">
                    <div class="stat-icon">💰</div>
                    <div class="stat-info">
                      <span class="stat-value">¥{{ selectedMajor.averageSalary != null ? Number(selectedMajor.averageSalary).toFixed(2) : '-' }}</span>
                      <span class="stat-label">平均薪资</span>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </div>
        </div>
      </el-dialog>
    </div>
  </TechLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import TechLayout from '@/components/TechLayout.vue'
import { searchMajorsApi } from '@/api/college'

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
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
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
  margin-bottom: 10px;
}

.search-section p {
  font-size: 16px;
  color: var(--color-text-secondary);
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
  padding: 30px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.result-header span {
  color: var(--color-text-secondary);
  font-size: 14px;
}

.sort-select {
  width: 150px;
}

.major-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  overflow: visible;
}

.major-card {
  background: var(--color-bg-card);
  padding: 25px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid var(--color-border);
  min-width: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.major-card:hover {
  transform: translateY(-3px);
  border-color: var(--color-border-hover);
  box-shadow: 0 0 20px var(--color-border-glow);
}

.major-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
  gap: 8px;
  min-width: 0;
}

.major-header h3 {
  font-size: 18px;
  color: var(--color-text-primary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.major-badges {
  display: flex;
  gap: 5px;
  flex-shrink: 0;
}

.major-info .category {
  color: var(--color-accent-primary);
  font-size: 13px;
  margin-bottom: 8px;
}

.major-info .description {
  color: var(--color-text-secondary);
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.major-stats {
  display: flex;
  gap: 20px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid var(--color-border);
}

.stat-item {
  flex: 1;
  text-align: center;
  min-width: 0;
  overflow: hidden;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 5px;
}

.stat-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
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
  color: var(--color-text-muted);
  margin-bottom: 5px;
}

.detail-section p {
  font-size: 15px;
  color: var(--color-text-primary);
  line-height: 1.6;
}

.detail-stats {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 15px;
  background: var(--color-bg-input);
  padding: 15px;
  border-radius: 10px;
  border: 1px solid var(--color-border);
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
  color: var(--color-text-primary);
}

.stat-info .stat-label {
  font-size: 12px;
  color: var(--color-text-muted);
}

@media (max-width: 1200px) {
  .major-grid {
    grid-template-columns: repeat(2, 1fr);
  }
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

  .result-section {
    padding: 20px 15px;
  }

  .major-card {
    padding: 20px;
  }

  .major-stats {
    flex-wrap: wrap;
    gap: 12px;
  }

  .el-dialog {
    width: 95% !important;
  }
}
</style>
