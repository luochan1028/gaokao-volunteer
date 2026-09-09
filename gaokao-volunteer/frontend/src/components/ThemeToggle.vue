<template>
  <div class="theme-toggle">
    <el-dropdown trigger="click" @command="handleCommand">
      <div class="toggle-btn glass-card">
        <span class="theme-icon">{{ currentThemeInfo.icon }}</span>
        <span class="theme-name">{{ currentThemeInfo.name }}</span>
        <span class="dropdown-arrow">▾</span>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="(info, key) in themes"
            :key="key"
            :command="key"
            :class="{ active: key === currentTheme }"
          >
            <span class="dropdown-icon">{{ info.icon }}</span>
            <span class="dropdown-label">{{ info.name }}</span>
            <span class="dropdown-sub">{{ info.label }}</span>
            <span v-if="key === currentTheme" class="check-mark">✓</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useThemeStore } from '@/store/theme'

const themeStore = useThemeStore()

const themes = themeStore.themes
const currentTheme = computed(() => themeStore.currentTheme)
const currentThemeInfo = computed(() => themes[currentTheme.value] || themes.dark)

function handleCommand(key) {
  themeStore.setTheme(key)
}
</script>

<style scoped>
.theme-toggle {
  display: flex;
  align-items: center;
}

.toggle-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: var(--color-text-secondary);
  transition: all 0.3s ease;
  user-select: none;
}

.toggle-btn:hover {
  color: var(--color-accent-primary);
  border-color: var(--color-border-hover);
  box-shadow: 0 0 15px var(--color-border-glow);
}

.theme-icon {
  font-size: 16px;
  line-height: 1;
}

.theme-name {
  font-size: 12px;
  white-space: nowrap;
}

.dropdown-arrow {
  font-size: 10px;
  opacity: 0.6;
}

.dropdown-icon {
  margin-right: 8px;
  font-size: 14px;
}

.dropdown-label {
  font-size: 13px;
}

.dropdown-sub {
  margin-left: 8px;
  font-size: 11px;
  color: var(--color-text-muted);
  font-family: var(--font-mono);
}

.check-mark {
  margin-left: auto;
  color: var(--color-accent-primary);
  font-weight: bold;
}

:deep(.el-dropdown-menu__item.active) {
  color: var(--color-accent-primary) !important;
  background: color-mix(in srgb, var(--color-accent-primary) 8%, transparent) !important;
}
</style>
