<template>
  <TechLayout>
    <div class="register-page">
      <div class="register-container">
        <div class="register-form glass-card tech-border">
          <h2 class="heading-tech">用户注册</h2>
          <p class="form-subtitle">高考志愿填报平台</p>
          <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
            <el-form-item prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" prefix-icon="el-icon-phone" />
            </el-form-item>
            <el-form-item prop="verificationCode">
              <div class="code-input">
                <el-input v-model="form.verificationCode" placeholder="请输入验证码" />
                <el-button @click="sendCode" :disabled="countdown > 0">
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </el-button>
              </div>
              <div v-if="sentCode" class="demo-code-tip">
                <span class="demo-code-label">📱 Demo 验证码：</span>
                <span class="demo-code-value">{{ sentCode }}</span>
              </div>
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="el-icon-lock" />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="请确认密码" prefix-icon="el-icon-lock" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleRegister" :loading="loading" class="register-btn">注册</el-button>
            </el-form-item>
          </el-form>
          <div class="register-links">
            <router-link to="/login">已有账号？立即登录</router-link>
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
import { sendCodeApi } from '@/api/auth'
import TechLayout from '@/components/TechLayout.vue'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref(null)
const loading = ref(false)
const countdown = ref(0)
const sentCode = ref("")

const form = reactive({
  phone: '',
  verificationCode: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      }, trigger: 'blur'
    }
  ]
}

async function sendCode() {
  if (!form.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }

  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.error('手机号格式不正确')
    return
  }

  try {
    const response = await sendCodeApi(form.phone)
    // Demo模式：后端直接返回验证码
    const code = response.data.data
    sentCode.value = code
    ElMessage.success(`验证码已发送，Demo验证码：${code}`)

    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '验证码发送失败')
  }
}

async function handleRegister() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await authStore.register(form.phone, form.password, form.verificationCode)
      ElMessage.success('注册成功')
      router.push('/login')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '注册失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
  padding: 60px 20px;
}

.register-container {
  width: 100%;
  max-width: 420px;
}

.register-form {
  padding: 40px;
  text-align: center;
}

.register-form h2 {
  font-size: 28px;
  margin-bottom: 5px;
}

.form-subtitle {
  font-size: 14px;
  color: var(--color-text-muted);
  margin-bottom: 30px;
}

.demo-code-tip {
  margin-top: 8px;
  padding: 10px 12px;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.demo-code-label {
  color: var(--color-text-secondary);
}
.demo-code-value {
  color: var(--color-accent-primary);
  font-weight: 700;
  font-size: 16px;
  font-family: var(--font-mono);
  letter-spacing: 3px;
}

.code-input {
  display: flex;
  gap: 10px;
}

.code-input .el-input {
  flex: 1;
}

.register-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  border-radius: 8px;
}

.register-links {
  margin-top: 20px;
}

.register-links a {
  color: var(--color-accent-primary);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s ease;
}

.register-links a:hover {
  text-decoration: underline;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.3);
}
</style>
