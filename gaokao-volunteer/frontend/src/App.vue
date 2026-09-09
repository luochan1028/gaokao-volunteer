<template>
  <div class="app-container circuit-bg">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useThemeStore } from '@/store/theme'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.initTheme()
})
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  position: relative;
}
</style>
