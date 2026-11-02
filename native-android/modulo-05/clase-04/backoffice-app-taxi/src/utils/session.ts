import type { AdminDto } from '@/types/models'

const TOKEN_KEY = 'apptaxi.backoffice.token'
const ADMIN_KEY = 'apptaxi.backoffice.admin'

export function readToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function readAdmin(): AdminDto | null {
  const raw = localStorage.getItem(ADMIN_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as AdminDto
  } catch {
    return null
  }
}

export function writeSession(token: string, admin: AdminDto) {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(ADMIN_KEY, JSON.stringify(admin))
}

export function writeAdmin(admin: AdminDto) {
  localStorage.setItem(ADMIN_KEY, JSON.stringify(admin))
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(ADMIN_KEY)
}
