import { defineStore } from 'pinia'
import { ref } from 'vue'
import { connectRealtime, disconnectRealtime, type ConnectionState } from '@/realtime/socket'

export const useRealtimeStore = defineStore('realtime', () => {
  const state = ref<ConnectionState>('idle')

  function connect(token: string, onUnauthorized: () => void) {
    connectRealtime(token, {
      onState: (value) => {
        state.value = value
      },
      onUnauthorized
    })
  }

  function disconnect() {
    disconnectRealtime()
    state.value = 'idle'
  }

  return { state, connect, disconnect }
})
