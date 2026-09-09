import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const themes = {
    dark: { name: '暗色科技', icon: '🌙', label: 'Dark Tech' },
    light: { name: '亮色简约', icon: '☀️', label: 'Light Clean' },
    cyan: { name: '青色海洋', icon: '🌊', label: 'Cyan Ocean' },
    purple: { name: '紫色梦幻', icon: '🔮', label: 'Purple Dream' }
  }

  const currentTheme = ref(localStorage.getItem('theme') || 'dark')

  function applyTheme(theme) {
    const html = document.documentElement
    html.setAttribute('data-theme', theme)
    localStorage.setItem('theme', theme)
    currentTheme.value = theme
  }

  function setTheme(theme) {
    if (themes[theme]) {
      applyTheme(theme)
    }
  }

  function toggleTheme() {
    const keys = Object.keys(themes)
    const currentIndex = keys.indexOf(currentTheme.value)
    const nextIndex = (currentIndex + 1) % keys.length
    applyTheme(keys[nextIndex])
  }

  function initTheme() {
    applyTheme(currentTheme.value)
  }

  return {
    themes,
    currentTheme,
    setTheme,
    toggleTheme,
    initTheme
  }
})
