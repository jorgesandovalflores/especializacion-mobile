import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '@/api/admin'
import { clearSession, readAdmin, readToken, writeAdmin, writeSession } from '@/utils/session'
import type { AdminDto } from '@/types/models'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(readToken())
  const admin = ref<AdminDto | null>(readAdmin())

  const isAuthenticated = computed(() => !!token.value)

  async function login(email: string, password: string) {
    const response = await authApi.login(email, password)
    token.value = response.accessToken
    admin.value = response.admin
    writeSession(response.accessToken, response.admin)
  }

  async function refreshProfile() {
    if (!token.value) return
    const profile = await authApi.me()
    admin.value = profile
    writeAdmin(profile)
  }

  function updateAdmin(profile: AdminDto) {
    admin.value = profile
    writeAdmin(profile)
  }

  function logout() {
    token.value = null
    admin.value = null
    clearSession()
  }

  return { token, admin, isAuthenticated, login, refreshProfile, updateAdmin, logout }
})
