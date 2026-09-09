<template>
  <TechLayout>
    <div class="login-page">
      <div class="login-container">
        <div class="login-form glass-card tech-border">
          <h2 class="heading-tech">欢迎登录</h2>
          <p class="form-subtitle">高考志愿填报平台</p>
          <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
            <el-form-item prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" prefix-icon="el-icon-phone" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="el-icon-lock" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleLogin" :loading="loading" class="login-btn">登录</el-button>
            </el-form-item>
          </el-form>
          <div class="login-links">
            <router-link to="/register">还没有账号？立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </TechLayout>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import TechLayout from '@/components/TechLayout.vue'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  phone: '',
  password: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

async function handleLogin() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await authStore.login(form.phone, form.password)
      ElMessage.success('登录成功')
      router.push('/')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
  padding: 60px 20px;
}

.login-container {
  width: 100%;
  max-width: 420px;
}

.login-form {
  padding: 40px;
  text-align: center;
}

.login-form h2 {
  font-size: 28px;
  margin-bottom: 5px;
}

.form-subtitle {
  font-size: 14px;
  color: var(--color-text-muted);
  margin-bottom: 30px;
}

.login-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  border-radius: 8px;
}

.login-links {
  margin-top: 20px;
}

.login-links a {
  color: var(--color-accent-primary);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s ease;
}

.login-links a:hover {
  text-decoration: underline;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.3);
}
</style>
