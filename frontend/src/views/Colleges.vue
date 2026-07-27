
<template>
  <div class="colleges-page">
    <header class="header">
      <div class="container">
        <div class="logo">
          <h1>高考志愿填报平台</h1>
        </div>
        <nav class="nav">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/colleges" class="nav-link active">院校查询</router-link>
          <router-link to="/majors" class="nav-link">专业查询</router-link>
          <router-link to="/recommend" class="nav-link">智能推荐</router-link>
          <router-link v-if="!authStore.isLoggedIn" to="/login" class="nav-link btn-login">登录</router-link>
          <router-link v-if="authStore.isLoggedIn" to="/profile" class="nav-link">个人中心</router-link>
        </nav>
      </div>
    </header>

    <main class="main-content">
      <div class="container">
        <div class="search-section">
          <el-input v-model="searchQuery" placeholder="搜索院校名称" class="search-input" @keyup.enter="handleSearch">
            <template #append>
              <el-button @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>

        <div class="filter-section">
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
          <div v-for="college in colleges" :key="college.id" class="college-card" @click="goToDetail(college.id)">
            <div class="college-badges">
              <span v-if="college.is985" class="badge badge-985">985</span>
              <span v-if="college.is211" class="badge badge-211">211</span>
              <span v-if="college.isDoubleFirstClass" class="badge badge-double">双一流</span>
            </div>
            <h3>{{ college.name }}</h3>
            <p class="college-location">{{ college.province }} · {{ college.city }}</p>
            <p class="college-type">{{ getTypeName(college.type) }}</p>
            <div class="college-info">
              <span>满意度: {{ college.satisfactionScore || '-' }}</span>
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
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { searchCollegesApi, getAllProvincesApi } from '@/api/college'

const router = useRouter()
const authStore = useAuthStore()

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

.nav-link:hover, .nav-link.active {
  background: #667eea;
  color: white;
}

.btn-login {
  background: #667eea;
  color: white !important;
}

.main-content {
  padding: 30px 0;
}

.search-section {
  margin-bottom: 20px;
}

.search-input {
  width: 400px;
}

.filter-section {
  background: white;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 20px;
}

.colleges-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.college-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #eee;
}

.college-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
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

.college-card h3 {
  font-size: 16px;
  margin: 0 0 10px;
  color: #333;
}

.college-location {
  font-size: 13px;
  color: #666;
  margin: 0 0 5px;
}

.college-type {
  font-size: 12px;
  color: #999;
  margin: 0 0 15px;
}

.college-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 30px;
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
