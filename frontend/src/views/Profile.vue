<template>
  <div class="profile-page">
    <header class="header">
      <div class="container">
        <div class="logo">
          <h1>高考志愿填报平台</h1>
        </div>
        <nav class="nav">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/recommend" class="nav-link">智能推荐</router-link>
          <router-link to="/volunteer" class="nav-link">志愿模拟</router-link>
          <router-link to="/plans" class="nav-link">方案管理</router-link>
          <router-link to="/profile" class="nav-link active">个人中心</router-link>
        </nav>
      </div>
    </header>

    <main class="main-content">
      <div class="container">
        <div class="profile-container">
          <div class="profile-sidebar">
            <div class="user-card">
              <div class="avatar">
                <span>{{ user?.phone?.slice(-4) || '用户' }}</span>
              </div>
              <div class="user-info">
                <h3>{{ user?.nickname || '用户' }}</h3>
                <p>{{ getUserTypeLabel(user?.userType) }}</p>
              </div>
            </div>
            
            <div class="menu-list">
              <div
                v-for="item in menuItems"
                :key="item.key"
                class="menu-item"
                :class="{ active: activeMenu === item.key }"
                @click="activeMenu = item.key"
              >
                <span class="menu-icon">{{ item.icon }}</span>
                <span>{{ item.label }}</span>
              </div>
            </div>
          </div>

          <div class="profile-content">
            <div v-if="activeMenu === 'basic'" class="content-section">
              <div class="section-header">
                <h2>基本信息</h2>
                <el-button type="primary" size="small" @click="editMode = !editMode">
                  {{ editMode ? '取消' : '编辑' }}
                </el-button>
              </div>
              
              <div v-if="!editMode" class="info-list">
                <div class="info-row">
                  <span class="info-label">手机号</span>
                  <span class="info-value">{{ user?.phone }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">昵称</span>
                  <span class="info-value">{{ user?.nickname || '-' }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">用户类型</span>
                  <span class="info-value">{{ getUserTypeLabel(user?.userType) }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">所在省份</span>
                  <span class="info-value">{{ user?.province || '-' }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">选考科目</span>
                  <span class="info-value">{{ user?.selectedSubjects?.join('、') || '-' }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">高考年份</span>
                  <span class="info-value">{{ user?.examYear || '-' }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">理科/文科</span>
                  <span class="info-value">{{ user?.scienceOrArts === 'SCIENCE' ? '理科' : user?.scienceOrArts === 'ARTS' ? '文科' : '-' }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">高考分数</span>
                  <span class="info-value">{{ user?.score || '-' }}分</span>
                </div>
                <div class="info-row">
                  <span class="info-label">全省位次</span>
                  <span class="info-value">{{ user?.rank || '-' }}</span>
                </div>
              </div>

              <div v-else class="edit-form">
                <el-form :model="editForm" label-width="120px">
                  <el-form-item label="昵称">
                    <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
                  </el-form-item>
                  <el-form-item label="所在省份">
                    <el-select v-model="editForm.province" placeholder="请选择省份">
                      <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="选考科目">
                    <el-select v-model="editForm.selectedSubjects" multiple placeholder="请选择选考科目">
                      <el-option label="物理" value="物理" />
                      <el-option label="化学" value="化学" />
                      <el-option label="生物" value="生物" />
                      <el-option label="历史" value="历史" />
                      <el-option label="政治" value="政治" />
                      <el-option label="地理" value="地理" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="高考年份">
                    <el-input v-model="editForm.examYear" placeholder="请输入年份" />
                  </el-form-item>
                  <el-form-item label="理科/文科">
                    <el-select v-model="editForm.scienceOrArts" placeholder="请选择">
                      <el-option label="理科" value="SCIENCE" />
                      <el-option label="文科" value="ARTS" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="高考分数">
                    <el-input v-model.number="editForm.score" placeholder="请输入分数" />
                  </el-form-item>
                  <el-form-item label="全省位次">
                    <el-input v-model.number="editForm.rank" placeholder="请输入位次" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="saveInfo">保存</el-button>
                    <el-button @click="editMode = false">取消</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </div>

            <div v-if="activeMenu === 'password'" class="content-section">
              <div class="section-header">
                <h2>修改密码</h2>
              </div>
              <el-form :model="passwordForm" label-width="120px" class="edit-form">
                <el-form-item label="原密码">
                  <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" />
                </el-form-item>
                <el-form-item label="新密码">
                  <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
                </el-form-item>
                <el-form-item label="确认密码">
                  <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="changePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </div>

            <div v-if="activeMenu === 'plans'" class="content-section">
              <div class="section-header">
                <h2>我的方案</h2>
                <router-link to="/plans" class="view-all">查看全部</router-link>
              </div>
              <div class="plan-list" v-if="planList.length > 0">
                <div
                  v-for="plan in planList"
                  :key="plan.id"
                  class="plan-item"
                >
                  <div class="plan-info">
                    <h4>{{ plan.name }}</h4>
                    <p>{{ plan.totalItems }}个志愿 | {{ plan.chongCount }}冲/{{ plan.wenCount }}稳/{{ plan.baoCount }}保</p>
                  </div>
                  <div class="plan-date">
                    {{ formatDate(plan.createdAt) }}
                  </div>
                </div>
              </div>
              <div v-else class="empty-tip">
                <p>暂无志愿方案</p>
                <router-link to="/volunteer" class="btn-primary">去创建</router-link>
              </div>
            </div>

            <div v-if="activeMenu === 'history'" class="content-section">
              <div class="section-header">
                <h2>浏览历史</h2>
                <el-button size="small" @click="clearHistory">清空历史</el-button>
              </div>
              <div v-if="historyList.length > 0" class="history-list">
                <div
                  v-for="item in historyList"
                  :key="item.id"
                  class="history-item"
                >
                  <div class="history-type">
                    {{ item.type === 'COLLEGE' ? '🏛️' : '📚' }}
                  </div>
                  <div class="history-info">
                    <h4>{{ item.name }}</h4>
                    <p>{{ item.subName }}</p>
                  </div>
                  <div class="history-date">
                    {{ formatDate(item.viewTime) }}
                  </div>
                </div>
              </div>
              <div v-else class="empty-tip">
                <p>暂无浏览记录</p>
              </div>
            </div>

            <div v-if="activeMenu === 'settings'" class="content-section">
              <div class="section-header">
                <h2>系统设置</h2>
              </div>
              <div class="settings-list">
                <div class="setting-item">
                  <span>通知设置</span>
                  <el-switch />
                </div>
                <div class="setting-item">
                  <span>接收推荐消息</span>
                  <el-switch />
                </div>
                <div class="setting-item">
                  <span>数据备份提醒</span>
                  <el-switch />
                </div>
                <div class="setting-item">
                  <span>清除缓存</span>
                  <el-button size="small" @click="clearCache">清除</el-button>
                </div>
                <div class="setting-item">
                  <span>关于我们</span>
                  <el-button size="small" @click="showAbout">查看</el-button>
                </div>
              </div>
            </div>

            <div v-if="activeMenu === 'logout'" class="content-section">
              <div class="logout-card">
                <div class="logout-icon">📌</div>
                <h3>确认退出登录？</h3>
                <p>退出后您将需要重新登录才能访问个人功能</p>
                <div class="logout-actions">
                  <el-button @click="activeMenu = 'basic'">取消</el-button>
                  <el-button type="danger" @click="handleLogout">确认退出</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <footer class="footer">
      <div class="container">
        <p>&copy; 2024 高考志愿填报平台. All rights reserved.</p>
        <p>系统推荐结果仅作为参考，请以各省官方志愿系统为准</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { getUserInfoApi, updateUserInfoApi, changePasswordApi } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()

const activeMenu = ref('basic')
const editMode = ref(false)
const user = ref(null)
const planList = ref([])
const historyList = ref([])

const provinces = ['北京', '上海', '广东', '浙江', '江苏', '山东', '四川', '湖北', '湖南', '河南', '河北', '安徽']

const menuItems = [
  { key: 'basic', label: '基本信息', icon: '👤' },
  { key: 'password', label: '修改密码', icon: '🔐' },
  { key: 'plans', label: '我的方案', icon: '📋' },
  { key: 'history', label: '浏览历史', icon: '📖' },
  { key: 'settings', label: '系统设置', icon: '⚙️' },
  { key: 'logout', label: '退出登录', icon: '🚪' }
]

const editForm = reactive({})
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const getUserTypeLabel = (type) => {
  const types = {
    'STUDENT': '应届高考生',
    'PARENT': '考生家长',
    'TEACHER': '升学规划老师',
    'SCHOOL': '高中学校',
    'REPEAT_STUDENT': '复读生',
    'VOCATIONAL': '中职毕业生'
  }
  return types[type] || '普通用户'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const loadUserInfo = async () => {
  try {
    const response = await getUserInfoApi()
    user.value = response.data.data
    
    editForm.nickname = user.value.nickname || ''
    editForm.province = user.value.province || ''
    editForm.selectedSubjects = user.value.selectedSubjects || []
    editForm.examYear = user.value.examYear || ''
    editForm.scienceOrArts = user.value.scienceOrArts || ''
    editForm.score = user.value.score || null
    editForm.rank = user.value.rank || null
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const saveInfo = async () => {
  try {
    const response = await updateUserInfoApi(editForm)
    user.value = response.data.data
    editMode.value = false
    alert('保存成功')
  } catch (error) {
    console.error('保存信息失败:', error)
    alert('保存失败')
  }
}

const changePassword = async () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    alert('两次输入的密码不一致')
    return
  }
  
  try {
    await changePasswordApi(passwordForm.oldPassword, passwordForm.newPassword)
    alert('密码修改成功，请重新登录')
    authStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('修改密码失败:', error)
    alert('修改密码失败')
  }
}

const clearHistory = () => {
  if (confirm('确定要清空浏览历史吗？')) {
    historyList.value = []
  }
}

const clearCache = () => {
  if (confirm('确定要清除缓存吗？')) {
    localStorage.clear()
    alert('缓存已清除')
  }
}

const showAbout = () => {
  alert('高考志愿填报平台 v1.0\n\n基于历年录取数据，为您提供专业、精准、个性化的志愿填报服务。')
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f7fa;
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

.main-content {
  padding: 40px 0;
}

.profile-container {
  display: flex;
  gap: 30px;
}

.profile-sidebar {
  width: 260px;
  flex-shrink: 0;
}

.user-card {
  background: white;
  border-radius: 15px;
  padding: 25px;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  color: white;
  font-size: 28px;
  font-weight: 600;
}

.user-info h3 {
  font-size: 18px;
  color: #333;
  margin: 0 0 5px;
}

.user-info p {
  font-size: 13px;
  color: #667eea;
  margin: 0;
}

.menu-list {
  background: white;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 20px;
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 1px solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover {
  background: #f5f7fa;
}

.menu-item.active {
  background: #667eea;
  color: white;
}

.menu-icon {
  font-size: 18px;
}

.profile-content {
  flex: 1;
}

.content-section {
  background: white;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.section-header h2 {
  font-size: 20px;
  color: #333;
  margin: 0;
}

.view-all {
  font-size: 14px;
  color: #667eea;
  text-decoration: none;
}

.info-list {
  max-width: 600px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-label {
  font-size: 14px;
  color: #666;
}

.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.edit-form {
  max-width: 600px;
}

.plan-list {
  margin-top: 15px;
}

.plan-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 10px;
  margin-bottom: 10px;
}

.plan-info h4 {
  font-size: 15px;
  color: #333;
  margin: 0 0 5px;
}

.plan-info p {
  font-size: 13px;
  color: #666;
  margin: 0;
}

.plan-date {
  font-size: 12px;
  color: #999;
}

.empty-tip {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty-tip p {
  margin-bottom: 20px;
}

.btn-primary {
  background: #667eea;
  color: white;
  padding: 10px 24px;
  border-radius: 20px;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-primary:hover {
  background: #5a6fd6;
}

.history-list {
  margin-top: 15px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 10px;
  margin-bottom: 10px;
}

.history-type {
  font-size: 24px;
}

.history-info h4 {
  font-size: 15px;
  color: #333;
  margin: 0 0 5px;
}

.history-info p {
  font-size: 13px;
  color: #666;
  margin: 0;
}

.history-date {
  margin-left: auto;
  font-size: 12px;
  color: #999;
}

.settings-list {
  margin-top: 15px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-item span {
  font-size: 14px;
  color: #333;
}

.logout-card {
  text-align: center;
  padding: 40px;
}

.logout-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.logout-card h3 {
  font-size: 20px;
  color: #333;
  margin: 0 0 10px;
}

.logout-card p {
  font-size: 14px;
  color: #666;
  margin: 0 0 30px;
}

.logout-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
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
  .profile-container {
    flex-direction: column;
  }
  
  .profile-sidebar {
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