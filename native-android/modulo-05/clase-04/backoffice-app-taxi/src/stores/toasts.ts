import { defineStore } from 'pinia'
import { ref } from 'vue'

export type ToastKind = 'success' | 'error' | 'info'

export interface Toast {
  id: number
  kind: ToastKind
  message: string
}

export const useToastStore = defineStore('toasts', () => {
  const items = ref<Toast[]>([])
  let sequence = 0

  function dismiss(id: number) {
    items.value = items.value.filter((toast) => toast.id !== id)
  }

  function push(kind: ToastKind, message: string, timeout = 4000) {
    const id = ++sequence
    items.value = [...items.value, { id, kind, message }].slice(-4)
    window.setTimeout(() => dismiss(id), timeout)
  }

  return {
    items,
    dismiss,
    success: (message: string) => push('success', message),
    error: (message: string) => push('error', message, 6000),
    info: (message: string) => push('info', message)
  }
})
