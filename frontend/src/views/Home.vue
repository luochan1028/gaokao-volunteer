
<template>
  <div class="home-page">
    <header class="header">
      <div class="container">
        <div class="logo">
          <h1>高考志愿填报平台</h1>
          <p>专业、精准、个性化的志愿填报服务</p>
        </div>
        <nav class="nav">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/colleges" class="nav-link">院校查询</router-link>
          <router-link to="/majors" class="nav-link">专业查询</router-link>
          <router-link to="/recommend" class="nav-link">智能推荐</router-link>
          <router-link v-if="!authStore.isLoggedIn" to="/login" class="nav-link btn-login">登录</router-link>
          <router-link v-if="!authStore.isLoggedIn" to="/register" class="nav-link btn-register">注册</router-link>
          <router-link v-if="authStore.isLoggedIn" to="/profile" class="nav-link">个人中心</router-link>
        </nav>
      </div>
    </header>

    <main class="main-content">
      <section class="hero">
        <div class="container">
          <h2>让志愿填报更科学</h2>
          <p>基于历年录取数据，为您智能生成"冲、稳、保"志愿梯度方案</p>
          <div class="hero-buttons">
            <router-link to="/recommend" class="btn-primary">立即开始</router-link>
            <router-link to="/colleges" class="btn-secondary">浏览院校</router-link>
          </div>
        </div>
      </section>

      <section class="features">
        <div class="container">
          <h3>核心功能</h3>
          <div class="feature-grid">
            <div class="feature-card">
              <div class="feature-icon">🎓</div>
              <h4>院校查询</h4>
              <p>覆盖全国3000+所高校，多维度筛选</p>
            </div>
            <div class="feature-card">
              <div class="feature-icon">📚</div>
              <h4>专业详情</h4>
              <p>2000+专业信息，就业前景分析</p>
            </div>
            <div class="feature-card">
              <div class="feature-icon">🤖</div>
              <h4>智能推荐</h4>
              <p>基于位次精准推荐，冲稳保分层</p>
            </div>
            <div class="feature-card">
              <div class="feature-icon">📊</div>
              <h4>风险评估</h4>
              <p>滑档概率、浪费分数指数计算</p>
            </div>
            <div class="feature-card">
              <div class="feature-icon">📝</div>
              <h4>志愿模拟</h4>
              <p>多方案对比，一键导出</p>
            </div>
            <div class="feature-card">
              <div class="feature-icon">💡</div>
              <h4>生涯测评</h4>
              <p>专业倾向测评，职业规划指导</p>
            </div>
          </div>
        </div>
      </section>

      <section class="quick-entry">
        <div class="container">
          <h3>快捷入口</h3>
          <div class="entry-grid">
            <router-link to="/recommend" class="entry-item">
              <div class="entry-icon">🎯</div>
              <span>智能推荐</span>
            </router-link>
            <router-link to="/colleges" class="entry-item">
              <div class="entry-icon">🏛️</div>
              <span>查院校</span>
            </router-link>
            <router-link to="/majors" class="entry-item">
              <div class="entry-icon">📖</div>
              <span>看专业</span>
            </router-link>
            <router-link to="/volunteer" class="entry-item">
              <div class="entry-icon">📋</div>
              <span>志愿模拟</span>
            </router-link>
            <router-link to="/plans" class="entry-item">
              <div class="entry-icon">📊</div>
              <span>方案对比</span>
            </router-link>
          </div>
        </div>
      </section>
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
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
</script>

<style scoped>
.home-page {
  min-height: 100vh;
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

.logo p {
  font-size: 12px;
  color: #666;
  margin: 5px 0 0;
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

.nav-link:hover {
  background: #f0f0f0;
  color: #667eea;
}

.btn-login {
  background: #667eea;
  color: white !important;
}

.btn-login:hover {
  background: #5a6fd6 !important;
}

.btn-register {
  border: 2px solid #667eea;
  color: #667eea !important;
}

.btn-register:hover {
  background: #667eea !important;
  color: white !important;
}

.hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 80px 0;
  text-align: center;
  color: white;
}

.hero h2 {
  font-size: 42px;
  margin-bottom: 20px;
}

.hero p {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 40px;
}

.hero-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.btn-primary {
  background: white;
  color: #667eea;
  padding: 12px 32px;
  border-radius: 30px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.2);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 12px 32px;
  border-radius: 30px;
  text-decoration: none;
  font-weight: 600;
  border: 2px solid white;
  transition: all 0.3s;
}

.btn-secondary:hover {
  background: white;
  color: #667eea;
}

.features {
  padding: 60px 0;
  background: white;
}

.features h3 {
  text-align: center;
  font-size: 28px;
  margin-bottom: 40px;
  color: #333;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
}

.feature-card {
  background: #f8f9fa;
  padding: 30px;
  border-radius: 15px;
  text-align: center;
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.feature-card h4 {
  font-size: 18px;
  margin-bottom: 10px;
  color: #333;
}

.feature-card p {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.quick-entry {
  padding: 60px 0;
  background: #f8f9fa;
}

.quick-entry h3 {
  text-align: center;
  font-size: 28px;
  margin-bottom: 40px;
  color: #333;
}

.entry-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

.entry-item {
  background: white;
  padding: 30px 20px;
  border-radius: 15px;
  text-align: center;
  text-decoration: none;
  color: #333;
  transition: all 0.3s;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.entry-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.2);
}

.entry-icon {
  font-size: 36px;
  margin-bottom: 10px;
}

.footer {
  background: #333;
  color: white;
  padding: 30px 0;
  text-align: center;
}

.footer p {
  margin: 5px 0;
  font-size: 14px;
  opacity: 0.8;
}

@media (max-width: 768px) {
  .feature-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .entry-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .hero h2 {
    font-size: 28px;
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
