
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, registerApi, getUserInfoApi, updateUserInfoApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(phone, password) {
    try {
      const response = await loginApi(phone, password)
      token.value = response.data.data.token
      user.value = response.data.data.user
      localStorage.setItem('token', token.value)
      return response
    } catch (error) {
      throw error
    }
  }

  async function register(phone, password, verificationCode) {
    try {
      const response = await registerApi(phone, password, verificationCode)
      return response
    } catch (error) {
      throw error
    }
  }

  async function getUserInfo() {
    try {
      const response = await getUserInfoApi()
      user.value = response.data.data
      return response
    } catch (error) {
      throw error
    }
  }

  async function updateInfo(data) {
    try {
      const response = await updateUserInfoApi(data)
      user.value = response.data.data
      return response
    } catch (error) {
      throw error
    }
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    user,
    isLoggedIn,
    login,
    register,
    getUserInfo,
    updateInfo,
    logout
  }
})
