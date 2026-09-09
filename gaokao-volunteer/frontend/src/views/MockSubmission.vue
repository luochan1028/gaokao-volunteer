<template>
  <TechLayout>
    <div class="mock-submission-page">
    <div class="page-header">
      <h1 class="heading-tech">模拟填报提交</h1>
      <p class="subtitle">MOCK SUBMISSION · 完整填报流程闭环</p>
    </div>

    <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom:24px">
      <el-step title="提交前校验" description="检查志愿表合规性" />
      <el-step title="确认志愿表" description="逐项确认志愿信息" />
      <el-step title="短信验证" description="模拟短信验证码" />
      <el-step title="提交完成" description="生成提交凭证" />
    </el-steps>

    <!-- Step 0: 提交前校验 -->
    <el-card v-if="currentStep === 0" shadow="hover">
      <template #header><span style="font-weight:bold">自动校验结果</span></template>
      <div v-loading="checking">
        <el-result v-if="checkResult" :icon="checkResult.canSubmit ? 'success' : 'error'"
          :title="checkResult.canSubmit ? '校验通过，可以提交' : '存在错误，无法提交'">
          <template #sub-title>
            <div>志愿总数: {{ checkResult.totalItems }} | 冲: {{ checkResult.gradient.chong }} 稳: {{ checkResult.gradient.wen }} 保: {{ checkResult.gradient.bao }}</div>
          </template>
        </el-result>

        <div v-if="checkResult && checkResult.errors.length" class="check-list">
          <el-alert v-for="(e, i) in checkResult.errors" :key="i" :title="e.message" type="error" show-icon :closable="false" style="margin-bottom:8px" />
        </div>
        <div v-if="checkResult && checkResult.warnings.length" class="check-list">
          <el-alert v-for="(w, i) in checkResult.warnings" :key="i" :title="w.message" type="warning" show-icon :closable="false" style="margin-bottom:8px" />
        </div>

        <div class="step-actions">
          <el-button @click="goToVolunteer">返回修改</el-button>
          <el-button type="primary" @click="currentStep = 1" :disabled="!checkResult || !checkResult.canSubmit">下一步</el-button>
        </div>
      </div>
    </el-card>

    <!-- Step 1: 确认志愿表 -->
    <el-card v-if="currentStep === 1" shadow="hover">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span style="font-weight:bold">确认志愿表</span>
          <el-tag type="info">提交次数剩余: 3/3</el-tag>
        </div>
      </template>
      <el-alert title="请仔细核对以下志愿信息，提交后将在模拟系统中生成正式凭证" type="info" show-icon :closable="false" style="margin-bottom:16px" />
      <el-table :data="confirmList" border style="width:100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="志愿名称" />
        <el-table-column prop="gradientType" label="梯度" width="80">
          <template #default="{ row }">
            <el-tag :type="gradientTag(row.gradientType)">{{ gradientText(row.gradientType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="probability" label="录取概率" width="100" align="center">
          <template #default="{ row }">{{ row.probability != null ? Number(row.probability * 100).toFixed(2) + '%' : '-' }}</template>
        </el-table-column>
        <el-table-column label="确认" width="80" align="center">
          <template #default="{ row }">
            <el-checkbox v-model="row.confirmed" />
          </template>
        </el-table-column>
      </el-table>
      <div class="step-actions">
        <el-button @click="currentStep = 0">上一步</el-button>
        <el-button type="primary" @click="goToVerify" :disabled="!allConfirmed">下一步</el-button>
      </div>
    </el-card>

    <!-- Step 2: 短信验证 -->
    <el-card v-if="currentStep === 2" shadow="hover" style="max-width:500px;margin:0 auto">
      <template #header><span style="font-weight:bold">短信验证码确认</span></template>
      <el-alert title="Demo模式下，请输入验证码：123456" type="warning" show-icon :closable="false" style="margin-bottom:16px" />
      <el-form>
        <el-form-item label="手机号">
          <el-input :model-value="maskedPhone" disabled />
        </el-form-item>
        <el-form-item label="验证码">
          <el-input v-model="verifyCode" placeholder="请输入6位验证码" maxlength="6" style="width:200px" />
          <el-button @click="sendCode" :disabled="countdown > 0" style="margin-left:8px">
            {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="step-actions">
        <el-button @click="currentStep = 1">上一步</el-button>
        <el-button type="primary" @click="doSubmit" :loading="submitting">确认提交</el-button>
      </div>
    </el-card>

    <!-- Step 3: 提交成功 -->
    <el-card v-if="currentStep === 3" shadow="hover" style="max-width:600px;margin:0 auto">
      <el-result icon="success" title="志愿提交成功！" sub-title="您的志愿已成功提交至模拟系统">
        <template #extra>
          <div class="submit-certificate">
            <el-descriptions :column="1" border title="提交凭证">
              <el-descriptions-item label="提交编号">{{ submitResult.submitNo }}</el-descriptions-item>
              <el-descriptions-item label="提交时间">{{ submitResult.submitTime }}</el-descriptions-item>
              <el-descriptions-item label="考生姓名">{{ submitResult.studentName }}</el-descriptions-item>
              <el-descriptions-item label="准考证号">{{ submitResult.admissionTicket }}</el-descriptions-item>
              <el-descriptions-item label="志愿数量">{{ submitResult.totalVolunteers }}个</el-descriptions-item>
              <el-descriptions-item label="剩余提交次数">{{ submitResult.remainingSubmissions }}次</el-descriptions-item>
            </el-descriptions>
          </div>
          <div style="margin-top:16px">
            <el-button @click="goToVolunteer">返回修改志愿</el-button>
            <el-button type="primary" @click="goToReport">查看志愿报告</el-button>
          </div>
        </template>
      </el-result>
    </el-card>
    </div>
  </TechLayout>
</template>

<script setup>
import TechLayout from '@/components/TechLayout.vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { preCheckSubmissionApi, submitVolunteerApi } from '@/api/submission'
import { getUserVolunteerListApi } from '@/api/volunteer'

const router = useRouter()
const authStore = useAuthStore()
const currentStep = ref(0)
const checking = ref(false)
const submitting = ref(false)
const checkResult = ref(null)
const confirmList = ref([])
const verifyCode = ref('')
const countdown = ref(0)
const submitResult = ref({})

const maskedPhone = computed(() => {
  const phone = authStore.user?.phone || '139****0000'
  return phone.length === 11 ? phone.slice(0,3) + '****' + phone.slice(7) : phone
})

const allConfirmed = computed(() => confirmList.value.length > 0 && confirmList.value.every(i => i.confirmed))

function gradientText(t) {
  return { CHONG: '冲', WEN: '稳', BAO: '保' }[t] || t
}

function gradientTag(t) {
  return { CHONG: 'danger', WEN: 'warning', BAO: 'success' }[t] || 'info'
}

async function runCheck() {
  checking.value = true
  try {
    const res = await preCheckSubmissionApi(1)
    if (res.data.code === 200) {
      checkResult.value = res.data.data
    }
  } catch (e) {
    // Fallback if not logged in or no plan
    checkResult.value = {
      canSubmit: false,
      errors: [{ type: 'AUTH', message: '请先登录并创建志愿方案' }],
      warnings: [],
      totalItems: 0,
      gradient: { chong: 0, wen: 0, bao: 0 }
    }
  } finally {
    checking.value = false
  }
}

async function loadVolunteerList() {
  try {
    const res = await getUserVolunteerListApi()
    if (res.data.code === 200) {
      confirmList.value = (res.data.data || []).map(v => ({
        id: v.id,
        name: v.name,
        gradientType: v.gradientType,
        probability: v.probability,
        confirmed: false
      }))
    }
  } catch {
    confirmList.value = []
  }
}

function goToVerify() {
  currentStep.value = 2
}

function sendCode() {
  ElMessage.success('验证码已发送（Demo模式请输入: 123456）')
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

async function doSubmit() {
  if (verifyCode.value !== '123456') {
    ElMessage.error('验证码错误，请输入: 123456')
    return
  }
  submitting.value = true
  try {
    const res = await submitVolunteerApi(1, verifyCode.value)
    if (res.data.code === 200) {
      submitResult.value = res.data.data
      currentStep.value = 3
    }
  } catch (e) {
    ElMessage.error('提交失败：' + (e.response?.data?.message || '请先登录'))
  } finally {
    submitting.value = false
  }
}

function goToVolunteer() {
  router.push('/volunteer')
}

function goToReport() {
  router.push('/plans')
}

onMounted(async () => {
  await runCheck()
  await loadVolunteerList()
})
</script>

<style scoped>
.mock-submission-page { padding: 20px; max-width: 900px; margin: 0 auto; }
.page-header { text-align: center; margin-bottom: 24px; }
.page-header h1 { font-size: 32px; margin-bottom: 8px; font-family: var(--font-heading); }
.page-header .subtitle { color: var(--color-text-muted); font-size: 13px; font-family: var(--font-mono); letter-spacing: 1px; }
.step-actions { text-align: center; margin-top: 20px; }
.check-list { margin-top: 16px; }
</style>
