import axios, { AxiosError } from 'axios'
import { API_URL } from './config'
import { readToken } from '@/utils/session'
import type { ApiErrorBody } from '@/types/models'

type UnauthorizedHandler = () => void

let unauthorizedHandler: UnauthorizedHandler | null = null

export function onUnauthorized(handler: UnauthorizedHandler) {
  unauthorizedHandler = handler
}

export const http = axios.create({
  baseURL: API_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    'Accept-Language': 'es'
  }
})

http.interceptors.request.use((config) => {
  const token = readToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiErrorBody>) => {
    const isLogin = error.config?.url?.includes('/admin/auth/login')
    if (error.response?.status === 401 && !isLogin) unauthorizedHandler?.()
    return Promise.reject(error)
  }
)

export function apiErrorMessage(error: unknown, fallback = 'Ocurrió un error inesperado'): string {
  if (axios.isAxiosError<ApiErrorBody>(error)) {
    if (!error.response) return 'No se pudo conectar con el servidor'
    const body = error.response.data
    const message = Array.isArray(body?.message) ? body?.message[0] : body?.message
    if (message) return message
    const firstFieldError = body?.errors?.[0]?.message
    if (firstFieldError) return firstFieldError
    if (error.response.status === 401) return 'Tu sesión expiró, vuelve a ingresar'
    if (error.response.status === 403) return 'No tienes permisos para esta acción'
    if (error.response.status === 404) return 'No se encontró el recurso'
  }
  return fallback
}

export function apiFieldErrors(error: unknown): Record<string, string> {
  if (!axios.isAxiosError<ApiErrorBody>(error)) return {}
  const errors = error.response?.data?.errors ?? []
  return Object.fromEntries(errors.map((item) => [item.field, item.message]))
}
