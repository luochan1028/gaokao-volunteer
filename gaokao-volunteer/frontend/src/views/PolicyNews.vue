<template>
  <TechLayout>
    <div class="policy-news-page">
    <div class="page-header">
      <h1>政策公告与消息</h1>
      <p class="subtitle">填报时间、批次规则、注意事项</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="18">
        <el-card shadow="hover">
          <template #header>
            <div style="display:flex;align-items:center;justify-content:space-between">
              <span style="font-weight:bold">政策公告列表</span>
              <el-select v-model="provinceFilter" placeholder="全部省份" clearable style="width:120px" @change="loadData">
                <el-option label="四川" value="四川" />
                <el-option label="北京" value="北京" />
                <el-option label="全国" value="" />
              </el-select>
            </div>
          </template>

          <div v-loading="loading">
            <el-empty v-if="!policies.length" description="暂无政策公告" />
            <div v-for="p in policies" :key="p.id" class="policy-item" @click="showDetail(p)">
              <div class="policy-header">
                <el-tag :type="typeTag(p.policyType)" size="small">{{ typeLabel(p.policyType) }}</el-tag>
                <span class="policy-title">{{ p.title }}</span>
              </div>
              <div class="policy-content">{{ p.content.substring(0, 80) }}...</div>
              <div class="policy-meta">
                <span><el-icon><Calendar /></el-icon> {{ formatDate(p.publishDate) }}</span>
                <span>来源: {{ p.source }}</span>
                <span v-if="p.tags"><el-tag v-for="t in p.tags.split(',')" :key="t" size="small" type="info" style="margin-right:4px">{{ t }}</el-tag></span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="msg-center">
          <template #header><span style="font-weight:bold">消息中心</span></template>
          <div class="msg-item" v-for="msg in messages" :key="msg.id">
            <el-badge :type="msg.read ? 'info' : 'danger'" is-dot>
              <span class="msg-text">{{ msg.text }}</span>
            </el-badge>
            <div class="msg-time">{{ msg.time }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详情弹窗 -->
    <el-dialog v-model="dialogVisible" :title="currentPolicy?.title" width="60%">
      <div v-if="currentPolicy" class="policy-detail">
        <div class="detail-meta">
          <el-tag :type="typeTag(currentPolicy.policyType)">{{ typeLabel(currentPolicy.policyType) }}</el-tag>
          <span>发布时间: {{ formatDate(currentPolicy.publishDate) }}</span>
          <span>来源: {{ currentPolicy.source }}</span>
        </div>
        <el-divider />
        <div class="detail-content">{{ currentPolicy.content }}</div>
        <div v-if="currentPolicy.tags" class="detail-tags">
          <el-tag v-for="t in currentPolicy.tags.split(',')" :key="t" size="small">{{ t }}</el-tag>
        </div>
      </div>
    </el-dialog>
    </div>
  </TechLayout>
</template>

<script setup>
import TechLayout from '@/components/TechLayout.vue'
import { ref, onMounted } from 'vue'
import { Calendar } from '@element-plus/icons-vue'
import { getPoliciesApi } from '@/api/policy'

const loading = ref(false)
const policies = ref([])
const provinceFilter = ref('四川')
const dialogVisible = ref(false)
const currentPolicy = ref(null)

const messages = ref([
  { id: 1, text: '您的志愿草稿已自动保存', time: '2分钟前', read: false },
  { id: 2, text: '提交次数剩余2次', time: '1小时前', read: false },
  { id: 3, text: '系统维护通知：今晚22:00-23:00', time: '3小时前', read: true },
  { id: 4, text: '欢迎注册高考志愿填报平台', time: '1天前', read: true }
])

async function loadData() {
  loading.value = true
  try {
    const res = await getPoliciesApi(provinceFilter.value)
    if (res.data.code === 200) {
      policies.value = res.data.data || []
    }
  } catch (e) {
    policies.value = []
  } finally {
    loading.value = false
  }
}

function showDetail(p) {
  currentPolicy.value = p
  dialogVisible.value = true
}

function typeLabel(type) {
  return { EXAM_POLICY: '考试政策', ENROLLMENT_RULE: '录取规则', MAJOR_INTRO: '专业介绍', SCORE_LINE: '分数线', EMPLOYMENT_REPORT: '就业报告', OTHER: '其他' }[type] || type
}

function typeTag(type) {
  return { EXAM_POLICY: 'danger', ENROLLMENT_RULE: 'warning', MAJOR_INTRO: 'success', SCORE_LINE: 'primary', EMPLOYMENT_REPORT: '', OTHER: 'info' }[type] || 'info'
}

function formatDate(date) {
  if (!date) return ''
  return date.toString().substring(0, 10)
}

onMounted(() => loadData())
</script>

<style scoped>
.policy-news-page { padding: 20px; max-width: 1200px; margin: 0 auto; }
.page-header { text-align: center; margin-bottom: 24px; }
.page-header h1 { font-size: 32px; margin-bottom: 8px; font-family: var(--font-heading); }
.page-header .subtitle { color: var(--color-text-muted); font-size: 13px; font-family: var(--font-mono); letter-spacing: 1px; }
.policy-item { padding: 16px; border-bottom: 1px solid var(--color-border); cursor: pointer; transition: all 0.2s; }
.policy-item:hover { background: rgba(0, 212, 255, 0.05); }
.policy-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.policy-title { font-weight: bold; font-size: 16px; color: var(--color-text-primary); }
.policy-content { color: var(--color-text-secondary); font-size: 14px; line-height: 1.6; margin-bottom: 8px; }
.policy-meta { display: flex; gap: 16px; font-size: 13px; color: var(--color-text-muted); }
.msg-center .msg-item { padding: 10px 0; border-bottom: 1px solid var(--color-border); }
.msg-text { font-size: 14px; color: var(--color-text-secondary); }
.msg-time { font-size: 12px; color: var(--color-text-muted); margin-top: 4px; }
.policy-detail .detail-meta { display: flex; gap: 16px; align-items: center; font-size: 14px; color: var(--color-text-muted); }
.detail-content { line-height: 1.8; color: var(--color-text-primary); white-space: pre-wrap; }
.detail-tags { margin-top: 16px; display: flex; gap: 8px; }
</style>
