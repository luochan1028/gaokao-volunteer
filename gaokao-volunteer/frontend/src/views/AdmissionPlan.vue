<template>
  <TechLayout>
    <div class="admission-plan-page">
    <div class="page-header">
      <h1>招生计划查询</h1>
      <p class="subtitle">2026年模拟招生计划数据（Demo数据）</p>
    </div>

    <el-alert title="以下为2026年模拟计划数据，仅用于Demo演示" type="warning" show-icon :closable="false" style="margin-bottom:16px" />

    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="院校搜索">
          <el-input v-model="filters.majorName" placeholder="专业名称" clearable style="width:160px" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="省份">
          <el-select v-model="filters.province" placeholder="全部" clearable style="width:120px" @change="loadData">
            <el-option label="四川" value="四川" />
            <el-option label="北京" value="北京" />
            <el-option label="上海" value="上海" />
          </el-select>
        </el-form-item>
        <el-form-item label="批次">
          <el-select v-model="filters.batch" placeholder="全部" clearable style="width:140px" @change="loadData">
            <el-option label="本科一批" value="BATCH_A" />
            <el-option label="本科二批" value="BATCH_B" />
            <el-option label="提前批" value="EARLY_BATCH" />
          </el-select>
        </el-form-item>
        <el-form-item label="科类">
          <el-select v-model="filters.scienceOrArts" placeholder="全部" clearable style="width:120px" @change="loadData">
            <el-option label="理科" value="SCIENCE" />
            <el-option label="文科" value="ARTS" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" style="margin-top:16px">
      <el-table :data="plans" v-loading="loading" stripe style="width:100%" @row-click="onRowClick">
        <el-table-column prop="college.name" label="院校名称" width="180" />
        <el-table-column prop="college.province" label="省份" width="80" />
        <el-table-column prop="majorName" label="专业名称" width="160" />
        <el-table-column prop="majorCode" label="专业代码" width="100" />
        <el-table-column prop="batch" label="批次" width="100">
          <template #default="{ row }">{{ batchLabel(row.batch) }}</template>
        </el-table-column>
        <el-table-column prop="scienceOrArts" label="科类" width="80">
          <template #default="{ row }">{{ row.scienceOrArts === 'SCIENCE' ? '理科' : '文科' }}</template>
        </el-table-column>
        <el-table-column prop="enrollmentCount" label="招生人数" width="100" align="center">
          <template #default="{ row }"><el-tag type="success">{{ Number(row.enrollmentCount || 0).toFixed(2) }}人</el-tag></template>
        </el-table-column>
        <el-table-column prop="schoolSystem" label="学制" width="60" align="center" />
        <el-table-column prop="tuition" label="学费" width="100" align="center">
          <template #default="{ row }">¥{{ Number(row.tuition || 0).toFixed(2) }}/年</template>
        </el-table-column>
        <el-table-column prop="subjectRequirements" label="选科要求" width="120">
          <template #default="{ row }"><el-tag size="small" type="warning">{{ row.subjectRequirements }}</el-tag></template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top:16px; justify-content:flex-end"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
    </div>
  </TechLayout>
</template>

<script setup>
import TechLayout from '@/components/TechLayout.vue'
import { ref, onMounted } from 'vue'
import { searchAdmissionPlansApi } from '@/api/admissionPlan'

const loading = ref(false)
const plans = ref([])
const page = ref(0)
const size = ref(10)
const total = ref(0)

const filters = ref({
  province: '四川',
  year: '2026',
  batch: '',
  scienceOrArts: 'SCIENCE',
  majorName: ''
})

async function loadData() {
  loading.value = true
  try {
    const res = await searchAdmissionPlansApi({
      province: filters.value.province || undefined,
      year: '2026',
      batch: filters.value.batch || undefined,
      scienceOrArts: filters.value.scienceOrArts || undefined,
      page: page.value,
      size: size.value
    })
    if (res.data.code === 200) {
      plans.value = res.data.data.content
      total.value = res.data.data.totalElements
    }
  } catch (e) {
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = { province: '四川', year: '2026', batch: '', scienceOrArts: '', majorName: '' }
  page.value = 0
  loadData()
}

function batchLabel(batch) {
  const labels = { BATCH_A: '本科一批', BATCH_B: '本科二批', BATCH_C: '本科三批', EARLY_BATCH: '提前批', SPECIAL_BATCH: '特殊批' }
  return labels[batch] || batch
}

function onRowClick(row) {
  // Navigate to college detail
}

onMounted(() => loadData())
</script>

<style scoped>
.admission-plan-page { padding: 20px; max-width: 1400px; margin: 0 auto; }
.page-header { text-align: center; margin-bottom: 16px; }
.page-header h1 { font-size: 32px; margin-bottom: 8px; font-family: var(--font-heading); }
.page-header .subtitle { color: var(--color-text-muted); font-size: 13px; font-family: var(--font-mono); letter-spacing: 1px; }
</style>
