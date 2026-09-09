
<template>
  <TechLayout>
    <div class="colleges-page">
      <div class="container">
        <h2 class="heading-tech page-title">院校查询</h2>

        <div class="search-section">
          <el-input v-model="searchQuery" placeholder="搜索院校名称" class="search-input" @keyup.enter="handleSearch">
            <template #append>
              <el-button @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>

        <div class="filter-section glass-card tech-border">
          <el-form :inline="true" :model="filters">
            <el-form-item label="省份">
              <el-select v-model="filters.province" placeholder="全部">
                <el-option label="全部" value="" />
                <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
            <el-form-item label="院校类型">
              <el-select v-model="filters.type" placeholder="全部">
                <el-option label="全部" value="" />
                <el-option label="公办" value="PUBLIC" />
                <el-option label="民办" value="PRIVATE" />
                <el-option label="独立学院" value="INDEPENDENT" />
                <el-option label="职业院校" value="VOCATIONAL" />
              </el-select>
            </el-form-item>
            <el-form-item label="院校层次">
              <el-select v-model="filters.level" placeholder="全部">
                <el-option label="全部" value="" />
                <el-option label="大学" value="UNIVERSITY" />
                <el-option label="学院" value="COLLEGE" />
                <el-option label="研究院" value="INSTITUTE" />
                <el-option label="职业学院" value="VOCATIONAL_COLLEGE" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="filters.is985">985</el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="filters.is211">211</el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="filters.isDoubleFirstClass">双一流</el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">筛选</el-button>
              <el-button @click="resetFilters">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="colleges-grid">
          <div v-for="college in colleges" :key="college.id" class="college-card glass-card tech-border" @click="goToDetail(college.id)">
            <div class="college-badges">
              <span v-if="college.is985" class="badge badge-985">985</span>
              <span v-if="college.is211" class="badge badge-211">211</span>
              <span v-if="college.isDoubleFirstClass" class="badge badge-double">双一流</span>
            </div>
            <h3>{{ college.name }}</h3>
            <p class="college-location">{{ college.province }} · {{ college.city }}</p>
            <p class="college-type">{{ getTypeName(college.type) }}</p>
            <div class="college-info">
              <span>满意度: {{ college.satisfactionScore != null ? Number(college.satisfactionScore).toFixed(2) : '-' }}</span>
              <span>在校生: {{ college.studentCount || '-' }}</span>
            </div>
          </div>
        </div>

        <div class="pagination-section">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[10, 20, 30, 50]"
            :page-size="pageSize"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
          />
        </div>
      </div>
    </div>
  </TechLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import TechLayout from '@/components/TechLayout.vue'
import { searchCollegesApi, getAllProvincesApi } from '@/api/college'

const router = useRouter()

const searchQuery = ref('')
const provinces = ref([])
const colleges = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const filters = reactive({
  province: '',
  type: '',
  level: '',
  is985: false,
  is211: false,
  isDoubleFirstClass: false
})

onMounted(() => {
  loadProvinces()
  handleSearch()
})

async function loadProvinces() {
  try {
    const response = await getAllProvincesApi()
    provinces.value = response.data.data
  } catch (error) {
    console.error('加载省份失败', error)
  }
}

async function handleSearch() {
  const params = {
    name: searchQuery.value || null,
    province: filters.province || null,
    type: filters.type || null,
    level: filters.level || null,
    is985: filters.is985 ? true : null,
    is211: filters.is211 ? true : null,
    isDoubleFirstClass: filters.isDoubleFirstClass ? true : null,
    page: currentPage.value - 1,
    size: pageSize.value
  }

  try {
    const response = await searchCollegesApi(params)
    colleges.value = response.data.data.content
    total.value = response.data.data.totalElements
  } catch (error) {
    console.error('搜索院校失败', error)
  }
}

function resetFilters() {
  filters.province = ''
  filters.type = ''
  filters.level = ''
  filters.is985 = false
  filters.is211 = false
  filters.isDoubleFirstClass = false
  currentPage.value = 1
  handleSearch()
}

function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  handleSearch()
}

function handleCurrentChange(page) {
  currentPage.value = page
  handleSearch()
}

function goToDetail(id) {
  router.push(`/colleges/${id}`)
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
</script>

<style scoped>
.colleges-page {
  min-height: 100vh;
  padding: 30px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 28px;
  margin: 0 0 24px;
}

.search-section {
  margin-bottom: 20px;
}

.search-input {
  max-width: 400px;
  width: 100%;
}

.filter-section {
  padding: 20px;
  margin-bottom: 20px;
}

.colleges-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  overflow: visible;
}

.college-card {
  padding: 20px;
  cursor: pointer;
  min-width: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.college-card:hover {
  transform: translateY(-3px);
}

.college-badges {
  display: flex;
  gap: 5px;
  margin-bottom: 10px;
}

.badge {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 10px;
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

.college-card h3 {
  font-size: 16px;
  margin: 0 0 10px;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.college-location {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin: 0 0 5px;
}

.college-type {
  font-size: 12px;
  color: var(--color-text-muted);
  margin: 0 0 15px;
}

.college-info {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  justify-content: space-between;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.college-info span {
  min-width: 0;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 1200px) {
  .colleges-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .colleges-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .search-input {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .colleges-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
