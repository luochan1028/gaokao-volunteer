<template>
  <TechLayout>
    <div class="home-page">
      <!-- Hero 区域 -->
      <section class="hero">
        <div class="hero-bg">
          <div class="hero-glow glow-1"></div>
          <div class="hero-glow glow-2"></div>
          <div class="hero-grid"></div>
        </div>
        <div class="container hero-content">
          <div class="hero-badge animate-fade-in-up">
            <span class="badge-dot"></span>
            <span>2026 志愿填报智能系统</span>
          </div>
          <h2 class="hero-title animate-fade-in-up stagger-1">
            <span class="heading-tech">让志愿填报</span>
            <span class="heading-tech">更科学</span>
          </h2>
          <p class="hero-subtitle animate-fade-in-up stagger-2">
            基于历年录取数据与位次分析，AI 智能生成「冲 · 稳 · 保」三梯度志愿方案
          </p>
          <div class="hero-buttons animate-fade-in-up stagger-3">
            <router-link to="/recommend" class="btn-primary-tech">
              <span>立即开始</span>
            </router-link>
            <router-link to="/colleges" class="btn-tech">
              <span>浏览院校</span>
            </router-link>
          </div>
          <div class="hero-stats animate-fade-in-up stagger-4">
            <div class="stat-item">
              <div class="stat-number heading-tech">50+</div>
              <div class="stat-label">模拟院校</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-number heading-tech">30+</div>
              <div class="stat-label">热门专业</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-number heading-tech">3年</div>
              <div class="stat-label">录取数据</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-number heading-tech">AI</div>
              <div class="stat-label">智能推荐</div>
            </div>
          </div>
        </div>
      </section>

      <!-- 核心功能 -->
      <section class="features">
        <div class="container">
          <div class="section-header">
            <div class="section-line"></div>
            <h3 class="heading-tech section-title">核心功能</h3>
            <div class="section-line"></div>
          </div>
          <div class="feature-grid">
            <div class="glass-card feature-card tech-border stagger-1" v-for="(f, i) in features" :key="i">
              <div class="feature-icon-wrap">
                <span class="feature-icon">{{ f.icon }}</span>
              </div>
              <h4>{{ f.title }}</h4>
              <p>{{ f.desc }}</p>
              <div class="feature-glow"></div>
            </div>
          </div>
        </div>
      </section>

      <!-- 快捷入口 -->
      <section class="quick-entry">
        <div class="container">
          <div class="section-header">
            <div class="section-line"></div>
            <h3 class="heading-tech section-title">快捷入口</h3>
            <div class="section-line"></div>
          </div>
          <div class="entry-grid">
            <router-link v-for="(e, i) in visibleEntries" :key="i" :to="e.link" class="entry-item glass-card tech-border" :class="'stagger-' + ((i % 5) + 1)">
              <div class="entry-icon-wrap">
                <span class="entry-icon">{{ e.icon }}</span>
              </div>
              <span class="entry-label">{{ e.label }}</span>
              <div class="entry-arrow">→</div>
            </router-link>
          </div>
        </div>
      </section>
    </div>
  </TechLayout>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/store/auth'
import TechLayout from '@/components/TechLayout.vue'

const authStore = useAuthStore()

const features = [
  { icon: '🎓', title: '院校查询', desc: '覆盖全国 50+ 所模拟院校，多维度筛选' },
  { icon: '📚', title: '专业详情', desc: '30+ 热门专业信息，就业前景分析' },
  { icon: '🤖', title: '智能推荐', desc: '基于位次精准推荐，冲稳保分层' },
  { icon: '📊', title: '风险评估', desc: '滑档概率、梯度失衡风险检测' },
  { icon: '📝', title: '志愿模拟', desc: '多方案对比，模拟提交凭证' },
  { icon: '💡', title: '政策公告', desc: '填报时间、批次规则实时更新' }
]

const entries = computed(() => [
  { icon: '🎯', label: '智能推荐', link: '/recommend', show: true },
  { icon: '🏛️', label: '查院校', link: '/colleges', show: true },
  { icon: '📖', label: '看专业', link: '/majors', show: true },
  { icon: '📋', label: '招生计划', link: '/admission-plans', show: true },
  { icon: '🔍', label: '志愿筛选', link: '/volunteer-filter', show: authStore.isLoggedIn },
  { icon: '📝', label: '志愿编排', link: '/volunteer', show: authStore.isLoggedIn },
  { icon: '⚠️', label: '风险评估', link: '/risk-assessment', show: authStore.isLoggedIn },
  { icon: '✅', label: '模拟提交', link: '/submit', show: authStore.isLoggedIn },
  { icon: '📢', label: '政策公告', link: '/policies', show: true },
  { icon: '📊', label: '方案管理', link: '/plans', show: authStore.isLoggedIn }
])

const visibleEntries = computed(() => entries.value.filter(e => e.show))
</script>

<style scoped>
.home-page { min-height: 100vh; position: relative; }

.container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }

/* ========== Hero 区域 ========== */
.hero {
  position: relative; padding: 80px 0 100px; overflow: hidden;
}
.hero-bg { position: absolute; top: 0; left: 0; right: 0; bottom: 0; }
.hero-glow {
  position: absolute; border-radius: 50%; filter: blur(80px);
}
.glow-1 { top: -100px; left: 10%; width: 400px; height: 400px; background: var(--color-border); }
.glow-2 { bottom: -100px; right: 10%; width: 500px; height: 500px; background: color-mix(in srgb, var(--color-accent-secondary) 12%, transparent); }
.hero-grid {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background-image:
    linear-gradient(color-mix(in srgb, var(--color-accent-primary) 5%, transparent) 1px, transparent 1px),
    linear-gradient(90deg, color-mix(in srgb, var(--color-accent-primary) 5%, transparent) 1px, transparent 1px);
  background-size: 50px 50px;
  mask-image: radial-gradient(ellipse at center, black 0%, transparent 70%);
  -webkit-mask-image: radial-gradient(ellipse at center, black 0%, transparent 70%);
}
.hero-content { position: relative; z-index: 1; text-align: center; }

.hero-badge {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 6px 16px; border-radius: 999px;
  background: color-mix(in srgb, var(--color-accent-primary) 8%, transparent); border: 1px solid var(--color-border);
  color: var(--color-accent-primary); font-size: 12px; font-family: var(--font-mono);
  margin-bottom: 24px;
}
.badge-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: var(--color-accent-green);
  animation: glow-pulse 2s ease-in-out infinite;
}
.hero-title {
  font-size: 48px; font-family: var(--font-heading); font-weight: 800;
  margin-bottom: 20px; display: flex; flex-direction: column; gap: 4px;
}
.hero-subtitle {
  font-size: 16px; color: var(--color-text-secondary);
  max-width: 600px; margin: 0 auto 40px; line-height: 1.8;
}
.hero-buttons { display: flex; gap: 16px; justify-content: center; margin-bottom: 60px; }

.hero-stats {
  display: flex; justify-content: center; align-items: center; gap: 24px;
}
.stat-item { text-align: center; }
.stat-number { font-size: 28px; font-family: var(--font-heading); font-weight: 700; }
.stat-label { font-size: 12px; color: var(--color-text-muted); margin-top: 4px; }
.stat-divider { width: 1px; height: 32px; background: var(--color-border); }

/* ========== 核心功能 ========== */
.features { padding: 80px 0; }
.section-header { display: flex; align-items: center; justify-content: center; gap: 20px; margin-bottom: 48px; }
.section-line { height: 1px; width: 60px; background: linear-gradient(90deg, transparent, var(--color-accent-primary), transparent); }
.section-title { font-size: 28px; font-family: var(--font-heading); font-weight: 700; }

.feature-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; }
.feature-card {
  padding: 32px 24px; text-align: center; cursor: default;
  position: relative; overflow: hidden;
}
.feature-icon-wrap {
  width: 64px; height: 64px; margin: 0 auto 16px;
  border-radius: 16px; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, color-mix(in srgb, var(--color-accent-primary) 10%, transparent), color-mix(in srgb, var(--color-accent-secondary) 10%, transparent));
  border: 1px solid var(--color-border);
  transition: all 0.3s ease;
}
.feature-card:hover .feature-icon-wrap {
  transform: scale(1.1);
  box-shadow: 0 0 20px var(--color-border-glow);
}
.feature-icon { font-size: 28px; }
.feature-card h4 { font-size: 18px; color: var(--color-text-primary); margin-bottom: 8px; }
.feature-card p { font-size: 14px; color: var(--color-text-secondary); line-height: 1.6; }
.feature-glow {
  position: absolute; bottom: -50%; left: -50%; width: 200%; height: 200%;
  background: radial-gradient(circle, color-mix(in srgb, var(--color-accent-primary) 5%, transparent) 0%, transparent 60%);
  opacity: 0; transition: opacity 0.5s ease;
}
.feature-card:hover .feature-glow { opacity: 1; }

/* ========== 快捷入口 ========== */
.quick-entry { padding: 60px 0 80px; }
.entry-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 20px; }
.entry-item {
  padding: 24px 16px; text-decoration: none; color: var(--color-text-primary);
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  text-align: center; position: relative;
}
.entry-item:hover { color: var(--color-text-primary); }
.entry-icon-wrap {
  width: 52px; height: 52px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, color-mix(in srgb, var(--color-accent-primary) 10%, transparent), color-mix(in srgb, var(--color-accent-secondary) 10%, transparent));
  border: 1px solid var(--color-border);
  transition: all 0.3s ease;
}
.entry-item:hover .entry-icon-wrap { transform: scale(1.1); box-shadow: 0 0 15px var(--color-border-glow); }
.entry-icon { font-size: 24px; }
.entry-label { font-size: 14px; font-weight: 500; }
.entry-arrow {
  position: absolute; top: 12px; right: 12px;
  color: var(--color-accent-primary); font-size: 14px; opacity: 0;
  transition: all 0.3s ease;
}
.entry-item:hover .entry-arrow { opacity: 1; transform: translateX(4px); }

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .feature-grid { grid-template-columns: repeat(2, 1fr); }
  .hero-title { font-size: 32px; }
  .hero-stats { flex-wrap: wrap; gap: 16px; }
  .stat-divider { display: none; }
}
</style>
