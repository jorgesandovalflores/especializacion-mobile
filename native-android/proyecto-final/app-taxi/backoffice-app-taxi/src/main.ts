import { createApp } from 'vue'
import { createPinia } from 'pinia'
import 'leaflet/dist/leaflet.css'
import './styles/main.css'
import App from './App.vue'
import { router } from './router'
import { onUnauthorized } from './api/http'
import { useAuthStore } from './stores/auth'
import { useRealtimeStore } from './stores/realtime'
import { useToastStore } from './stores/toasts'
import './composables/useTheme'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)

onUnauthorized(() => {
  const auth = useAuthStore()
  if (!auth.isAuthenticated) return
  auth.logout()
  useRealtimeStore().disconnect()
  useToastStore().error('Tu sesión expiró, vuelve a ingresar')
  const current = router.currentRoute.value
  router.replace({ name: 'login', query: current.meta.public ? {} : { redirect: current.fullPath } })
})

app.mount('#app')
