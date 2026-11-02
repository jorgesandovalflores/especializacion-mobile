import { onBeforeUnmount, onMounted } from 'vue'
import { subscribe, type RealtimeEventName, type RealtimeHandler } from '@/realtime/socket'

export function useRealtime<E extends RealtimeEventName>(event: E, handler: RealtimeHandler<E>) {
  let unsubscribe: (() => void) | null = null
  onMounted(() => {
    unsubscribe = subscribe(event, handler)
  })
  onBeforeUnmount(() => {
    unsubscribe?.()
  })
}
