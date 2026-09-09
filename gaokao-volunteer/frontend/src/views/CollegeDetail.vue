
<template>
  <TechLayout>
    <div class="detail-page">
      <div class="container" v-if="college">
        <div class="back-link">
          <router-link to="/colleges">← 返回院校列表</router-link>
        </div>

        <div class="college-header glass-card tech-border">
          <div class="college-badges">
            <span v-if="college.is985" class="badge badge-985">985</span>
            <span v-if="college.is211" class="badge badge-211">211</span>
            <span v-if="college.isDoubleFirstClass" class="badge badge-double">双一流</span>
          </div>
          <h2 class="heading-tech">{{ college.name }}</h2>
          <p>{{ college.province }} · {{ college.city }} · {{ getTypeName(college.type) }}</p>
        </div>

        <div class="info-section">
          <el-row :gutter="20">
            <el-col :span="16">
              <div class="info-card glass-card tech-border">
                <h3>院校简介</h3>
                <p>{{ college.description || '暂无简介' }}</p>
              </div>

              <div class="info-card glass-card tech-border">
                <h3>院校特色</h3>
                <p>{{ college.features || '暂无特色信息' }}</p>
              </div>

              <div class="info-card glass-card tech-border">
                <h3>优势学科</h3>
                <p>{{ college.advantages || '暂无优势学科信息' }}</p>
              </div>
            </el-col>

            <el-col :span="8">
              <div class="stats-card glass-card tech-border">
                <h3>院校数据</h3>
                <div class="stat-item">
                  <span class="stat-label">满意度</span>
                  <span class="stat-value">{{ college.satisfactionScore != null ? Number(college.satisfactionScore).toFixed(2) : '-' }}</span>
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

              <div class="actions-card tech-border">
                <el-button type="primary" @click="addToVolunteer" class="action-btn">加入志愿列表</el-button>
                <a :href="college.website" target="_blank" class="website-link">访问官网</a>
              </div>
            </el-col>
          </el-row>
        </div>

        <div class="tabs-section glass-card tech-border">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="开设专业" name="majors">
              <div class="majors-list">
                <div v-for="major in majors" :key="major.id" class="major-item">
                  <div class="major-info">
                    <h4>{{ major.name }}</h4>
                    <p>{{ getCategoryName(major.category) }} · 满意度: {{ Number(major.satisfactionScore || 0).toFixed(2) }}</p>
                  </div>
                  <div class="major-salary">
                    平均薪资: {{ major.averageSalary != null ? Number(major.averageSalary).toFixed(2) + '元' : '-' }}
                  </div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="历年分数线" name="scores">
              <div class="scores-list">
                <el-table :data="scores" border>
                  <el-table-column prop="year" label="年份" />
                  <el-table-column prop="majorName" label="专业" />
                  <el-table-column label="最低分">
                    <template #default="{ row }">{{ row.minScore != null ? Number(row.minScore).toFixed(2) : '-' }}</template>
                  </el-table-column>
                  <el-table-column label="最高分">
                    <template #default="{ row }">{{ row.maxScore != null ? Number(row.maxScore).toFixed(2) : '-' }}</template>
                  </el-table-column>
                  <el-table-column label="平均分">
                    <template #default="{ row }">{{ row.averageScore != null ? Number(row.averageScore).toFixed(2) : '-' }}</template>
                  </el-table-column>
                  <el-table-column label="最低位次">
                    <template #default="{ row }">{{ row.minRank != null ? Number(row.minRank).toFixed(2) : '-' }}</template>
                  </el-table-column>
                  <el-table-column label="最高位次">
                    <template #default="{ row }">{{ row.maxRank != null ? Number(row.maxRank).toFixed(2) : '-' }}</template>
                  </el-table-column>
                </el-table>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </TechLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import TechLayout from '@/components/TechLayout.vue'
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
  padding: 30px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.back-link {
  margin-bottom: 20px;
}

.back-link a {
  color: var(--color-accent-primary);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s ease;
}

.back-link a:hover {
  text-shadow: 0 0 10px var(--color-border-hover);
}

.college-header {
  padding: 30px;
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
  font-family: var(--font-mono);
  font-weight: 600;
}

.badge-985 {
  background: color-mix(in srgb, var(--color-accent-pink) 20%, transparent);
  color: var(--color-accent-pink);
  border: 1px solid color-mix(in srgb, var(--color-accent-pink) 30%, transparent);
}

.badge-211 {
  background: color-mix(in srgb, var(--color-accent-yellow) 20%, transparent);
  color: var(--color-accent-yellow);
  border: 1px solid color-mix(in srgb, var(--color-accent-yellow) 30%, transparent);
}

.badge-double {
  background: color-mix(in srgb, var(--color-accent-primary) 20%, transparent);
  color: var(--color-accent-primary);
  border: 1px solid var(--color-border-hover);
}

.college-header h2 {
  font-size: 32px;
  margin: 0 0 10px;
}

.college-header p {
  font-size: 16px;
  color: var(--color-text-secondary);
  margin: 0;
}

.info-card {
  padding: 25px;
  margin-bottom: 20px;
}

.info-card h3 {
  font-size: 18px;
  margin: 0 0 15px;
  color: var(--color-text-primary);
}

.info-card p {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.8;
  margin: 0;
  word-break: break-word;
  overflow-wrap: break-word;
}

.stats-card {
  padding: 25px;
  margin-bottom: 20px;
}

.stats-card h3 {
  font-size: 18px;
  margin: 0 0 20px;
  color: var(--color-text-primary);
}

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.stat-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.actions-card {
  background: linear-gradient(135deg, var(--color-accent-primary), var(--color-accent-secondary));
  padding: 25px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 0 20px var(--color-border-glow);
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
  padding: 20px;
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
  border-bottom: 1px solid var(--color-border);
}

.major-item:last-child {
  border-bottom: none;
}

.major-info {
  min-width: 0;
  flex: 1;
  overflow: hidden;
}

.major-info h4 {
  font-size: 16px;
  margin: 0 0 5px;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.major-info p {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin: 0;
}

.major-salary {
  font-size: 14px;
  color: var(--color-accent-primary);
  font-weight: 600;
  flex-shrink: 0;
}

.scores-list {
  margin-top: 20px;
  overflow-x: auto;
}

/* Element Plus Tabs 暗色覆盖 */
:deep(.el-tabs__header) {
  background: transparent;
  border-bottom: 1px solid var(--color-border);
  margin: 0 0 20px;
}

:deep(.el-tabs__item) {
  color: var(--color-text-secondary);
}

:deep(.el-tabs__item:hover) {
  color: var(--color-accent-primary);
}

:deep(.el-tabs__item.is-active) {
  color: var(--color-accent-primary);
}

:deep(.el-tabs__active-bar) {
  background: var(--color-accent-primary);
  box-shadow: 0 0 8px var(--color-border-glow);
}

:deep(.el-tabs__content) {
  color: var(--color-text-primary);
}

@media (max-width: 992px) {
  .info-section :deep(.el-col-8),
  .info-section :deep(.el-col-16) {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .college-header h2 {
    font-size: 24px;
  }
}
</style>
