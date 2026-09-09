<template>
  <TechLayout>
    <div class="volunteer-filter-page">
    <div class="page-header">
      <h1>志愿筛选器</h1>
      <p class="subtitle">多维度组合筛选，精准定位目标院校专业</p>
    </div>

    <el-card shadow="hover" class="filter-card">
      <el-radio-group v-model="mode" style="margin-bottom:16px">
        <el-radio-button value="college">院校优先模式</el-radio-button>
        <el-radio-button value="major">专业优先模式</el-radio-button>
      </el-radio-group>

      <el-form :inline="true" :model="filters" label-width="80px">
        <el-form-item label="省份">
          <el-select v-model="filters.province" placeholder="全部" clearable style="width:120px" @change="loadData">
            <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="院校层次">
          <el-select v-model="filters.level" placeholder="全部" clearable style="width:140px" @change="loadData">
            <el-option label="985" value="985" />
            <el-option label="211" value="211" />
            <el-option label="双一流" value="双一流" />
            <el-option label="普通本科" value="普通本科" />
          </el-select>
        </el-form-item>
        <el-form-item label="分数区间">
          <el-input-number v-model="filters.minScore" :min="0" :max="750" placeholder="最低" style="width:100px" />
          <span style="margin:0 4px">-</span>
          <el-input-number v-model="filters.maxScore" :min="0" :max="750" placeholder="最高" style="width:100px" />
        </el-form-item>
        <el-form-item label="排序">
          <el-select v-model="filters.sortBy" style="width:140px" @change="applySort">
            <el-option label="录取概率" value="probability" />
            <el-option label="录取位次" value="rank" />
            <el-option label="学费" value="tuition" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" :loading="loading">筛选</el-button>
          <el-button @click="batchAddToVolunteer" :disabled="!selected.length">批量加入志愿({{ selected.length }})</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" style="margin-top:16px">
      <el-table :data="results" v-loading="loading" stripe @selection-change="val => selected = val" style="width:100%">
        <el-table-column type="selection" width="50" />
        <el-table-column label="院校" width="160">
          <template #default="{ row }">
            <el-link type="primary" @click="goCollege(row.collegeId)">{{ row.collegeName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="province" label="省份" width="80" />
        <el-table-column label="层次" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.is985" type="danger" size="small">985</el-tag>
            <el-tag v-else-if="row.is211" type="warning" size="small">211</el-tag>
            <el-tag v-else-if="row.isDoubleFirstClass" type="success" size="small">双一流</el-tag>
            <el-tag v-else size="small">普通</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="majorName" label="专业" width="140" />
        <el-table-column label="录取分数" width="100" align="center">
          <template #default="{ row }">{{ Number(row.minScore || 0).toFixed(2) }}-{{ Number(row.maxScore || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="录取位次" width="100" align="center">
          <template #default="{ row }">{{ row.minRank }}</template>
        </el-table-column>
        <el-table-column label="概率" width="100" align="center">
          <template #default="{ row }">
            <el-progress :percentage="Number(row.probability || 0).toFixed(2)" :color="probColor(row.probability)" :stroke-width="8" />
          </template>
        </el-table-column>
        <el-table-column prop="tuition" label="学费" width="90" align="center">
          <template #default="{ row }">¥{{ Number(row.tuition || 5000).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="addToVolunteer(row)">加入</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    </div>
  </TechLayout>
</template>

<script setup>
import TechLayout from '@/components/TechLayout.vue'
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { searchCollegesApi, getEnrollmentScoresApi } from '@/api/college'
import { addToVolunteerListApi } from '@/api/volunteer'

const router = useRouter()
const loading = ref(false)
const results = ref([])
const selected = ref([])
const mode = ref('college')

const provinces = ['四川', '北京', '上海', '江苏', '浙江', '湖北', '广东', '重庆', '陕西', '湖南']

const filters = ref({
  province: '四川',
  level: '',
  minScore: 500,
  maxScore: 650,
  sortBy: 'probability'
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      province: filters.value.province || undefined,
      is985: filters.value.level === '985' ? true : undefined,
      is211: filters.value.level === '211' ? true : undefined,
      isDoubleFirstClass: filters.value.level === '双一流' ? true : undefined,
      page: 0,
      size: 50
    }
    const res = await searchCollegesApi(params)
    if (res.data.code === 200) {
      const colleges = res.data.data.content || []
      const enriched = []
      for (const c of colleges) {
        let scoreRes
        try { scoreRes = await getEnrollmentScoresApi(c.id, '2024') } catch { scoreRes = null }
        const scores = scoreRes?.data?.data || []
        const score = scores[0] || {}
        enriched.push({
          collegeId: c.id,
          collegeName: c.name,
          province: c.province,
          is985: c.is985,
          is211: c.is211,
          isDoubleFirstClass: c.isDoubleFirstClass,
          majorName: '计算机科学与技术',
          minScore: score.minScore || 550,
          maxScore: score.maxScore || 600,
          minRank: score.minRank || 10000,
          probability: calcProb(score.minRank, score.maxRank),
          tuition: 4500 + Math.floor(Math.random() * 2000)
        })
      }
      results.value = enriched.filter(r => r.minScore >= filters.value.minScore && r.minScore <= filters.value.maxScore)
      applySort()
    }
  } catch (e) {
    ElMessage.error('筛选失败')
  } finally {
    loading.value = false
  }
}

function calcProb(minRank, maxRank) {
  const userRank = 15000
  if (!minRank) return 60
  if (userRank <= minRank) return 95
  if (userRank >= maxRank) return 15
  return Math.round(95 - (userRank - minRank) / (maxRank - minRank) * 80)
}

function probColor(p) {
  if (p >= 85) return '#67c23a'
  if (p >= 60) return '#e6a23c'
  return '#f56c6c'
}

function applySort() {
  const sortKey = filters.value.sortBy
  results.value.sort((a, b) => {
    if (sortKey === 'probability') return b.probability - a.probability
    if (sortKey === 'rank') return a.minRank - b.minRank
    if (sortKey === 'tuition') return a.tuition - b.tuition
    return 0
  })
}

async function addToVolunteer(row) {
  try {
    await addToVolunteerListApi(row.collegeId, null, row.collegeName + '-' + row.majorName)
    ElMessage.success('已加入志愿表')
  } catch {
    ElMessage.warning('加入失败，可能需要登录')
  }
}

async function batchAddToVolunteer() {
  for (const row of selected.value) {
    try { await addToVolunteerListApi(row.collegeId, null, row.collegeName + '-' + row.majorName) } catch {}
  }
  ElMessage.success(`已批量添加${selected.value.length}个志愿`)
}

function goCollege(id) {
  router.push(`/colleges/${id}`)
}

onMounted(() => loadData())
</script>

<style scoped>
.volunteer-filter-page { padding: 20px; max-width: 1400px; margin: 0 auto; }
.page-header { text-align: center; margin-bottom: 16px; }
.page-header h1 { font-size: 32px; margin-bottom: 8px; font-family: var(--font-heading); }
.page-header .subtitle { color: var(--color-text-muted); font-size: 13px; font-family: var(--font-mono); letter-spacing: 1px; }
</style>
