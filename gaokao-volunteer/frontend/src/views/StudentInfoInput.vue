<template>
  <TechLayout>
    <div class="student-info-page">
    <div class="page-header">
      <h1 class="heading-tech">考生信息录入</h1>
      <p class="subtitle">STUDENT PROFILE · 完善信息，获取个性化推荐</p>
    </div>

    <el-row :gutter="24">
      <el-col :span="16">
        <el-card shadow="hover" class="form-card">
          <template #header>
            <div class="card-header">
              <el-icon><EditPen /></el-icon>
              <span>考生信息表单</span>
            </div>
          </template>

          <el-form :model="form" label-width="120px" label-position="right">
            <el-divider content-position="left">基础信息</el-divider>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="姓名" required>
                  <el-input v-model="form.name" placeholder="请输入姓名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号">
                  <el-input v-model="form.phone" placeholder="手机号" disabled />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="考生省份">
              <el-select v-model="form.province" placeholder="请选择省份" style="width:100%">
                <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>

            <el-divider content-position="left">成绩信息</el-divider>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="总分" required>
                  <el-input-number v-model="form.totalScore" :min="0" :max="750" :step="1" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="位次" required>
                  <el-input-number v-model="form.rank" :min="1" :step="1" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="科类">
                  <el-select v-model="form.scienceOrArts" style="width:100%">
                    <el-option label="物理类" value="SCIENCE" />
                    <el-option label="历史类" value="ARTS" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="选科组合">
              <el-checkbox-group v-model="form.selectedSubjects" :min="2" :max="3">
                <el-checkbox label="物理" value="物理" />
                <el-checkbox label="化学" value="化学" />
                <el-checkbox label="生物" value="生物" />
                <el-checkbox label="历史" value="历史" />
                <el-checkbox label="地理" value="地理" />
                <el-checkbox label="政治" value="政治" />
              </el-checkbox-group>
            </el-form-item>

            <el-divider content-position="left">偏好设置</el-divider>
            <el-form-item label="地域偏好">
              <el-select v-model="form.preferredProvinces" multiple placeholder="选择偏好省份" style="width:100%">
                <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
            <el-form-item label="专业偏好">
              <el-select v-model="form.preferredMajors" multiple placeholder="选择偏好专业类别" style="width:100%">
                <el-option label="工学" value="ENGINEERING" />
                <el-option label="理学" value="SCIENCE" />
                <el-option label="医学" value="MEDICINE" />
                <el-option label="法学" value="LAW" />
                <el-option label="文学" value="LITERATURE" />
                <el-option label="经济学" value="ECONOMICS" />
                <el-option label="管理学" value="MANAGEMENT" />
                <el-option label="教育学" value="EDUCATION" />
              </el-select>
            </el-form-item>
            <el-form-item label="院校层次">
              <el-checkbox-group v-model="form.collegeLevelPrefs">
                <el-checkbox label="985" value="985" />
                <el-checkbox label="211" value="211" />
                <el-checkbox label="双一流" value="双一流" />
                <el-checkbox label="普通本科" value="普通本科" />
              </el-checkbox-group>
            </el-form-item>

            <el-divider content-position="left">限制条件</el-divider>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="外语语种">
                  <el-select v-model="form.foreignLanguage" style="width:100%">
                    <el-option label="英语" value="英语" />
                    <el-option label="日语" value="日语" />
                    <el-option label="俄语" value="俄语" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="体检结论">
                  <el-select v-model="form.healthCondition" style="width:100%">
                    <el-option label="合格" value="合格" />
                    <el-option label="色弱" value="色弱" />
                    <el-option label="色盲" value="色盲" />
                    <el-option label="受限" value="受限" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="报考资格">
              <el-switch v-model="form.specialPlan" active-text="专项计划" />
              <el-switch v-model="form.artSports" active-text="艺术体育类" style="margin-left:20px" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveInfo" :loading="saving" size="large">
                保存信息并生成画像
              </el-button>
              <el-button @click="goToRecommend" size="large" style="margin-left:12px">
                前往智能推荐
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="profile-card" v-if="showProfile">
          <template #header>
            <div class="card-header">
              <el-icon><User /></el-icon>
              <span>您的画像标签</span>
            </div>
          </template>
          <div class="profile-tags">
            <el-tag v-for="tag in profileTags" :key="tag" :type="getTagType(tag)" size="large" class="profile-tag">
              {{ tag }}
            </el-tag>
          </div>
          <el-divider />
          <div class="profile-summary">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="姓名">{{ form.name }}</el-descriptions-item>
              <el-descriptions-item label="省份">{{ form.province }}</el-descriptions-item>
              <el-descriptions-item label="科类">{{ form.scienceOrArts === 'SCIENCE' ? '物理类' : '历史类' }}</el-descriptions-item>
              <el-descriptions-item label="总分">{{ Number(form.totalScore || 0).toFixed(2) }}分</el-descriptions-item>
              <el-descriptions-item label="位次">{{ Number(form.rank || 0).toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item label="选科">{{ form.selectedSubjects.join('·') }}</el-descriptions-item>
              <el-descriptions-item label="地域偏好">{{ form.preferredProvinces.join('、') || '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="专业偏好">{{ majorPrefLabels }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
        <el-empty v-else description="保存信息后显示考生画像" />
      </el-col>
    </el-row>
    </div>
  </TechLayout>
</template>

<script setup>
import TechLayout from '@/components/TechLayout.vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, User } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()
const saving = ref(false)
const showProfile = ref(false)

const provinces = ['四川', '北京', '上海', '江苏', '浙江', '湖北', '广东', '重庆', '陕西', '湖南', '山东', '福建', '安徽', '天津', '黑龙江', '甘肃', '贵州', '云南']

const form = ref({
  name: '',
  phone: '',
  province: '四川',
  totalScore: 550,
  rank: 15000,
  scienceOrArts: 'SCIENCE',
  selectedSubjects: ['物理', '化学'],
  preferredProvinces: ['四川'],
  preferredMajors: ['ENGINEERING'],
  collegeLevelPrefs: ['985', '211', '双一流'],
  foreignLanguage: '英语',
  healthCondition: '合格',
  specialPlan: false,
  artSports: false
})

const majorPrefLabels = computed(() => {
  const labels = { ENGINEERING: '工学', SCIENCE: '理学', MEDICINE: '医学', LAW: '法学', LITERATURE: '文学', ECONOMICS: '经济学', MANAGEMENT: '管理学', EDUCATION: '教育学' }
  return form.value.preferredMajors.map(m => labels[m] || m).join('、') || '未设置'
})

const profileTags = computed(() => {
  const tags = []
  tags.push(form.value.scienceOrArts === 'SCIENCE' ? '物理类' : '历史类')
  tags.push(`${form.value.totalScore}分`)
  tags.push(`位次${form.value.rank}`)
  if (form.value.selectedSubjects.length) tags.push(form.value.selectedSubjects.join('·'))
  if (form.value.preferredProvinces.length) tags.push(`偏好${form.value.preferredProvinces.join('、')}`)
  if (form.value.preferredMajors.length) {
    const labels = { ENGINEERING: '工学', SCIENCE: '理学', MEDICINE: '医学', LAW: '法学', LITERATURE: '文学', ECONOMICS: '经济学', MANAGEMENT: '管理学', EDUCATION: '教育学' }
    tags.push(`倾向${form.value.preferredMajors.map(m => labels[m]).join('、')}`)
  }
  if (form.value.collegeLevelPrefs.length) tags.push(form.value.collegeLevelPrefs.join('/'))
  if (form.value.specialPlan) tags.push('专项计划')
  if (form.value.artSports) tags.push('艺术体育')
  return tags
})

function getTagType(tag) {
  if (tag.includes('分') || tag.includes('位次')) return 'danger'
  if (tag.includes('物理') || tag.includes('历史')) return 'primary'
  if (tag.includes('偏好') || tag.includes('倾向')) return 'success'
  return 'info'
}

async function saveInfo() {
  if (!form.value.name) {
    ElMessage.warning('请输入姓名')
    return
  }
  saving.value = true
  try {
    const data = {
      name: form.value.name,
      province: form.value.province,
      scienceOrArts: form.value.scienceOrArts,
      totalScore: form.value.totalScore,
      rank: form.value.rank,
      selectedSubjects: form.value.selectedSubjects.join(','),
      subjectDetails: form.value.scienceOrArts === 'SCIENCE' ? '物理类' : '历史类',
      examYear: '2024'
    }
    await authStore.updateInfo(data)
    showProfile.value = true
    ElMessage.success('信息保存成功！考生画像已生成')
  } catch (e) {
    ElMessage.error('保存失败：' + (e.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

function goToRecommend() {
  router.push('/recommend')
}

onMounted(() => {
  if (authStore.user) {
    form.value.name = authStore.user.name || ''
    form.value.phone = authStore.user.phone || ''
    form.value.province = authStore.user.province || '四川'
    form.value.totalScore = authStore.user.totalScore || 550
    form.value.rank = authStore.user.rank || 15000
    form.value.scienceOrArts = authStore.user.scienceOrArts || 'SCIENCE'
    if (authStore.user.selectedSubjects) {
      form.value.selectedSubjects = authStore.user.selectedSubjects.split(',')
    }
    if (authStore.user.totalScore) showProfile.value = true
  }
})
</script>

<style scoped>
.student-info-page { padding: 20px; max-width: 1400px; margin: 0 auto; }
.page-header { text-align: center; margin-bottom: 24px; }
.page-header h1 { font-size: 32px; margin-bottom: 8px; font-family: var(--font-heading); }
.page-header .subtitle { color: var(--color-text-muted); font-size: 13px; font-family: var(--font-mono); letter-spacing: 1px; }
.form-card { margin-bottom: 20px; }
.card-header { display: flex; align-items: center; gap: 8px; font-weight: bold; }
.profile-card { position: sticky; top: 20px; }
.profile-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.profile-tag { margin: 0; }
</style>
