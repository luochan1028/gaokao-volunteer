<template>
  <div class="tech-layout">
    <header class="header">
      <div class="nav-container">
        <div class="logo" @click="router.push('/')">
          <div class="logo-icon">
            <span>GAOKAO</span>
          </div>
          <div class="logo-text">
            <h1 class="heading-tech">高考志愿填报</h1>
            <p>SMART VOLUNTEER PLATFORM</p>
          </div>
        </div>

        <button class="mobile-menu-btn" @click="mobileMenuOpen = !mobileMenuOpen">
          <span></span><span></span><span></span>
        </button>

        <nav class="nav" :class="{ open: mobileMenuOpen }">
          <router-link to="/" class="nav-link" @click="mobileMenuOpen = false">首页</router-link>
          <router-link to="/colleges" class="nav-link" @click="mobileMenuOpen = false">院校查询</router-link>
          <router-link to="/majors" class="nav-link" @click="mobileMenuOpen = false">专业查询</router-link>
          <router-link to="/admission-plans" class="nav-link" @click="mobileMenuOpen = false">招生计划</router-link>
          <router-link to="/recommend" class="nav-link" @click="mobileMenuOpen = false">智能推荐</router-link>
          <router-link to="/volunteer-filter" class="nav-link" v-if="authStore.isLoggedIn" @click="mobileMenuOpen = false">志愿筛选</router-link>
          <router-link to="/volunteer" class="nav-link" v-if="authStore.isLoggedIn" @click="mobileMenuOpen = false">志愿编排</router-link>
          <router-link to="/risk-assessment" class="nav-link" v-if="authStore.isLoggedIn" @click="mobileMenuOpen = false">风险评估</router-link>
          <router-link to="/policies" class="nav-link" @click="mobileMenuOpen = false">政策公告</router-link>
          <template v-if="!authStore.isLoggedIn">
            <router-link to="/login" class="nav-link btn-login" @click="mobileMenuOpen = false">登录</router-link>
            <router-link to="/register" class="nav-link btn-register" @click="mobileMenuOpen = false">注册</router-link>
          </template>
          <template v-else>
            <router-link to="/student-info" class="nav-link" @click="mobileMenuOpen = false">信息录入</router-link>
            <router-link to="/submit" class="nav-link" @click="mobileMenuOpen = false">模拟提交</router-link>
            <router-link to="/plans" class="nav-link" @click="mobileMenuOpen = false">方案管理</router-link>
            <router-link to="/profile" class="nav-link" @click="mobileMenuOpen = false">个人中心</router-link>
          </template>
        </nav>

        <div class="nav-actions">
          <ThemeToggle />
        </div>
      </div>
    </header>

    <main class="layout-main">
      <slot />
    </main>

    <footer class="footer">
      <div class="footer-container">
        <div class="footer-content">
          <div class="footer-logo heading-tech">高考志愿填报平台</div>
          <p>系统推荐结果仅作为参考，请以各省官方志愿系统为准</p>
          <p class="footer-copy">&copy; 2026 GAOKAO Volunteer Platform · Demo Version</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import ThemeToggle from '@/components/ThemeToggle.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const mobileMenuOpen = ref(false)

watch(() => route.path, () => {
  mobileMenuOpen.value = false
})
</script>

<style scoped>
.tech-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--color-header-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--color-border);
  transition: background 0.3s ease;
}

.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 24px;
  gap: 16px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  flex-shrink: 0;
}

.logo-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, var(--color-accent-primary), var(--color-accent-secondary));
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-heading);
  font-size: 10px;
  font-weight: 800;
  color: var(--logo-text-color);
  box-shadow: 0 0 20px var(--color-border-glow);
  transition: all 0.3s ease;
}

.logo:hover .logo-icon {
  box-shadow: 0 0 30px var(--color-border-glow);
  transform: scale(1.05);
}

.logo-text h1 {
  font-size: 18px;
  margin: 0;
  font-family: var(--font-heading);
}

.logo-text p {
  font-size: 9px;
  color: var(--color-text-muted);
  margin: 2px 0 0;
  letter-spacing: 1px;
}

.nav {
  display: flex;
  gap: 2px;
  align-items: center;
  flex-wrap: wrap;
  flex: 1;
  justify-content: center;
}

.nav-link {
  text-decoration: none;
  color: var(--color-text-secondary);
  font-size: 13px;
  padding: 7px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.nav-link:hover {
  color: var(--color-accent-primary);
  background: color-mix(in srgb, var(--color-accent-primary) 8%, transparent);
}

.nav-link.router-link-active {
  color: var(--color-accent-primary);
  background: color-mix(in srgb, var(--color-accent-primary) 10%, transparent);
}

.btn-login {
  background: linear-gradient(135deg, var(--color-accent-primary), var(--color-accent-secondary));
  color: #fff !important;
  font-weight: 600;
}

.btn-login:hover {
  box-shadow: 0 0 15px var(--color-border-glow);
}

.btn-register {
  border: 1px solid var(--color-accent-primary);
  color: var(--color-accent-primary) !important;
}

.btn-register:hover {
  background: color-mix(in srgb, var(--color-accent-primary) 10%, transparent);
}

.nav-actions {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.mobile-menu-btn {
  display: none;
  flex-direction: column;
  gap: 4px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 6px;
}

.mobile-menu-btn span {
  width: 22px;
  height: 2px;
  background: var(--color-text-primary);
  border-radius: 2px;
  transition: all 0.3s ease;
}

.layout-main {
  flex: 1;
  position: relative;
  z-index: 1;
}

.footer {
  background: var(--color-footer-bg);
  border-top: 1px solid var(--color-border);
  padding: 40px 24px;
  text-align: center;
  transition: background 0.3s ease;
}

.footer-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

.footer-logo {
  font-size: 20px;
  font-family: var(--font-heading);
  margin-bottom: 8px;
}

.footer p {
  font-size: 13px;
  color: var(--color-text-muted);
}

.footer-copy {
  font-family: var(--font-mono);
  font-size: 12px;
  opacity: 0.6;
}

/* 中等屏幕：导航栏滚动 */
@media (max-width: 1200px) {
  .nav {
    gap: 1px;
  }
  .nav-link {
    padding: 7px 10px;
    font-size: 12px;
  }
}

/* 小屏幕：汉堡菜单 */
@media (max-width: 768px) {
  .nav-container {
    flex-wrap: wrap;
    padding: 10px 16px;
  }

  .mobile-menu-btn {
    display: flex;
    margin-left: auto;
  }

  .nav {
    display: none;
    width: 100%;
    flex-direction: column;
    align-items: stretch;
    gap: 4px;
    order: 3;
  }

  .nav.open {
    display: flex;
    flex-direction: column;
    padding: 8px 0;
    max-height: 400px;
    overflow-y: auto;
  }

  .nav-link {
    padding: 10px 16px;
    text-align: left;
  }

  .nav-actions {
    order: 2;
  }
}
</style>
