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
            <span>2026 志愿填报 · AI 智能对话</span>
          </div>
          <h2 class="hero-title animate-fade-in-up stagger-1">
            <span class="heading-tech">用对话聊出</span>
            <span class="heading-tech">你的志愿方向</span>
          </h2>
          <p class="hero-subtitle animate-fade-in-up stagger-2">
            语音对话采集信息，张雪峰八问 + 霍兰德性格测评，AI 自动生成专属《志愿意向书》
          </p>
          <div class="hero-buttons animate-fade-in-up stagger-3">
            <router-link to="/ai/assistant" class="btn-primary-tech ai-hero-btn">
              <span class="btn-icon">🤖</span>
              <span>开始AI对话</span>
              <span class="btn-arrow">→</span>
            </router-link>
            <router-link to="/recommend" class="btn-tech">
              <span>传统推荐</span>
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
            <router-link to="/ai/assistant" class="glass-card feature-card feature-card-ai tech-border stagger-0">
              <div class="feature-icon-wrap ai-icon-wrap">
                <span class="feature-icon">🤖</span>
                <span class="ai-badge">NEW</span>
              </div>
              <h4>AI对话助手</h4>
              <p>语音聊志愿，张雪峰八问+性格测评，自动生成《志愿意向书》</p>
              <div class="feature-ai-tags">
                <span>语音对话</span>
                <span>八问决策</span>
                <span>意向书</span>
              </div>
              <div class="feature-glow"></div>
            </router-link>
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
            <router-link v-for="(e, i) in visibleEntries" :key="i" :to="e.link" class="entry-item glass-card tech-border" :class="['stagger-' + ((i % 5) + 1), { 'entry-highlight': e.highlight }]">
              <div class="entry-icon-wrap">
                <span class="entry-icon">{{ e.icon }}</span>
                <span v-if="e.highlight" class="entry-new-badge">NEW</span>
              </div>
              <span class="entry-label">{{ e.label }}</span>
              <div class="entry-arrow">→</div>
            </router-link>
          </div>
        </div>
      </section>

      <!-- 浮动AI助手按钮 -->
      <router-link to="/ai/assistant" class="floating-ai-btn" title="AI志愿助手">
        <span class="floating-icon">🤖</span>
        <span class="floating-pulse"></span>
        <span class="floating-tooltip">AI志愿助手</span>
      </router-link>
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
  { icon: '🤖', label: 'AI对话助手', link: '/ai/assistant', show: true, highlight: true },
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
.ai-hero-btn {
  display: inline-flex !important; align-items: center; gap: 10px;
  padding: 14px 32px !important; font-size: 16px !important; font-weight: 600 !important;
  position: relative; overflow: hidden;
}
.ai-hero-btn .btn-icon { font-size: 20px; }
.ai-hero-btn .btn-arrow {
  transition: transform 0.3s ease;
}
.ai-hero-btn:hover .btn-arrow { transform: translateX(4px); }

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

/* AI 功能卡片 - 突出样式 */
.feature-card-ai {
  cursor: pointer !important; text-decoration: none; color: inherit;
  background: linear-gradient(135deg,
    color-mix(in srgb, var(--color-accent-primary) 8%, var(--color-bg-card)),
    color-mix(in srgb, var(--color-accent-secondary) 5%, var(--color-bg-card))) !important;
  border-color: color-mix(in srgb, var(--color-accent-primary) 40%, var(--color-border)) !important;
  grid-column: span 1;
  position: relative;
}
.feature-card-ai::before {
  content: ''; position: absolute; top: 0; left: 0; right: 0; height: 3px;
  background: linear-gradient(90deg, var(--color-accent-primary), var(--color-accent-secondary));
  border-radius: 16px 16px 0 0;
}
.feature-card-ai:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px var(--color-border-glow);
}
.ai-icon-wrap {
  background: linear-gradient(135deg, var(--color-accent-primary), var(--color-accent-secondary)) !important;
  position: relative;
}
.ai-icon-wrap .feature-icon { filter: brightness(0) invert(1); }
.ai-badge {
  position: absolute; top: -6px; right: -6px;
  background: var(--color-accent-green); color: #fff;
  font-size: 10px; font-weight: bold; padding: 2px 6px; border-radius: 10px;
  animation: badge-bounce 2s ease-in-out infinite;
}
@keyframes badge-bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}
.feature-ai-tags {
  display: flex; gap: 6px; justify-content: center; flex-wrap: wrap;
  margin-top: 12px;
}
.feature-ai-tags span {
  padding: 2px 8px; border-radius: 12px; font-size: 11px;
  background: color-mix(in srgb, var(--color-accent-primary) 10%, transparent);
  color: var(--color-accent-primary); border: 1px solid color-mix(in srgb, var(--color-accent-primary) 20%, transparent);
}
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

/* 高亮快捷入口 */
.entry-highlight {
  background: linear-gradient(135deg,
    color-mix(in srgb, var(--color-accent-primary) 12%, var(--color-bg-card)),
    color-mix(in srgb, var(--color-accent-secondary) 8%, var(--color-bg-card))) !important;
  border-color: color-mix(in srgb, var(--color-accent-primary) 40%, var(--color-border)) !important;
  position: relative;
}
.entry-highlight .entry-icon-wrap {
  background: linear-gradient(135deg, var(--color-accent-primary), var(--color-accent-secondary)) !important;
}
.entry-highlight .entry-icon { filter: brightness(0) invert(1); }
.entry-new-badge {
  position: absolute; top: -4px; right: -4px;
  background: var(--color-accent-green); color: #fff;
  font-size: 9px; font-weight: bold; padding: 1px 5px; border-radius: 8px;
}

/* 浮动AI按钮 */
.floating-ai-btn {
  position: fixed; right: 24px; bottom: 80px; z-index: 1000;
  width: 64px; height: 64px; border-radius: 50%;
  background: linear-gradient(135deg, var(--color-accent-primary), var(--color-accent-secondary));
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 8px 30px color-mix(in srgb, var(--color-accent-primary) 40%, transparent);
  text-decoration: none;
  transition: all 0.3s ease;
}
.floating-ai-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 12px 40px color-mix(in srgb, var(--color-accent-primary) 50%, transparent);
}
.floating-icon {
  font-size: 28px;
  animation: float-bob 2s ease-in-out infinite;
}
@keyframes float-bob {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}
.floating-pulse {
  position: absolute; width: 100%; height: 100%; border-radius: 50%;
  background: color-mix(in srgb, var(--color-accent-primary) 30%, transparent);
  animation: floating-pulse 2s ease-out infinite;
}
@keyframes floating-pulse {
  0% { transform: scale(1); opacity: 0.6; }
  100% { transform: scale(1.8); opacity: 0; }
}
.floating-tooltip {
  position: absolute; right: 76px; white-space: nowrap;
  background: var(--color-bg-card); color: var(--color-text-primary);
  padding: 6px 14px; border-radius: 8px; font-size: 13px;
  border: 1px solid var(--color-border);
  opacity: 0; pointer-events: none; transition: opacity 0.3s ease;
}
.floating-ai-btn:hover .floating-tooltip { opacity: 1; }
</style>
