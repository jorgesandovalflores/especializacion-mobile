<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import AppIcon, { type IconName } from '@/components/AppIcon.vue'
import ConnectionIndicator from '@/components/ConnectionIndicator.vue'
import logoUrl from '@/assets/feature_splash_logo.png'
import { useAuthStore } from '@/stores/auth'
import { useRealtimeStore } from '@/stores/realtime'
import { useToastStore } from '@/stores/toasts'
import { useTheme } from '@/composables/useTheme'
import { http } from '@/api/http'

interface NavItem {
  to: string
  label: string
  icon: IconName
}

interface NavGroup {
  title: string
  items: NavItem[]
}

const groups: NavGroup[] = [
  {
    title: 'Monitoreo',
    items: [
      { to: '/map/drivers', label: 'Mapa de conductores', icon: 'map' },
      { to: '/map/trips', label: 'Mapa de viajes', icon: 'route' },
      { to: '/trips', label: 'Viajes', icon: 'list' }
    ]
  },
  {
    title: 'Gestión',
    items: [
      { to: '/drivers', label: 'Conductores', icon: 'car' },
      { to: '/passengers', label: 'Pasajeros', icon: 'users' },
      { to: '/admins', label: 'Administradores', icon: 'shield' },
      { to: '/tariff', label: 'Tarifa', icon: 'money' }
    ]
  }
]

const auth = useAuthStore()
const realtime = useRealtimeStore()
const toasts = useToastStore()
const router = useRouter()
const route = useRoute()
const { admin, token } = storeToRefs(auth)
const { theme, toggle } = useTheme()
const menuOpen = ref(false)
const contentEl = ref<HTMLElement | null>(null)

const initials = computed(() => {
  const name = admin.value?.fullName?.trim() || admin.value?.email || 'A'
  return name
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('')
})

const isActive = (to: string) => route.path === to || (to === '/trips' && route.path.startsWith('/trips/'))

function connectSocket() {
  if (!token.value) return
  realtime.connect(token.value, () => {
    http.get('/admin/me').catch(() => undefined)
  })
}

function logout() {
  realtime.disconnect()
  auth.logout()
  toasts.info('Sesión cerrada')
  router.replace({ name: 'login' })
}

watch(
  () => route.fullPath,
  () => {
    menuOpen.value = false
  }
)

watch(
  () => route.path,
  () => {
    contentEl.value?.scrollTo({ top: 0 })
  }
)

onMounted(() => {
  connectSocket()
  auth.refreshProfile().catch(() => undefined)
})

onBeforeUnmount(() => realtime.disconnect())
</script>

<template>
  <div class="shell" :class="{ 'menu-open': menuOpen }">
    <aside class="sidebar">
      <div class="sidebar-top">
        <RouterLink to="/" class="brand" aria-label="Inicio">
          <img :src="logoUrl" alt="App Taxi" class="brand-logo" />
          <span class="brand-caption">Backoffice</span>
        </RouterLink>
        <button class="sidebar-close" type="button" aria-label="Cerrar menú" @click="menuOpen = false">
          <AppIcon name="close" />
        </button>
      </div>

      <nav class="nav" aria-label="Navegación principal">
        <div v-for="group in groups" :key="group.title" class="nav-group">
          <p class="nav-title">{{ group.title }}</p>
          <RouterLink
            v-for="item in group.items"
            :key="item.to"
            :to="item.to"
            class="nav-link"
            :class="{ active: isActive(item.to) }"
          >
            <AppIcon :name="item.icon" />
            <span>{{ item.label }}</span>
          </RouterLink>
        </div>
      </nav>

      <div class="sidebar-bottom">
        <ConnectionIndicator class="sidebar-connection" />
        <div class="profile">
          <div class="avatar">{{ initials }}</div>
          <div class="profile-text">
            <strong>{{ admin?.fullName || 'Administrador' }}</strong>
            <span>{{ admin?.email }}</span>
          </div>
        </div>
        <div class="sidebar-actions">
          <button
            class="side-btn icon-only"
            type="button"
            :aria-label="theme === 'dark' ? 'Usar modo claro' : 'Usar modo oscuro'"
            :title="theme === 'dark' ? 'Modo claro' : 'Modo oscuro'"
            @click="toggle"
          >
            <AppIcon :name="theme === 'dark' ? 'sun' : 'moon'" :size="18" />
          </button>
          <button class="side-btn" type="button" @click="logout">
            <AppIcon name="logout" :size="18" />
            Cerrar sesión
          </button>
        </div>
      </div>
    </aside>

    <div class="backdrop" @click="menuOpen = false" />

    <div class="main">
      <header class="topbar">
        <button class="btn btn-ghost btn-icon" type="button" aria-label="Abrir menú" @click="menuOpen = true">
          <AppIcon name="menu" />
        </button>
        <img :src="logoUrl" alt="App Taxi" class="topbar-logo" />
        <ConnectionIndicator class="topbar-connection" />
      </header>
      <main ref="contentEl" class="content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.shell {
  display: grid;
  grid-template-columns: var(--sidebar-width) minmax(0, 1fr);
  height: 100%;
}

.sidebar {
  background: var(--sidebar-bg);
  color: #ffffff;
  display: flex;
  flex-direction: column;
  padding: 20px 16px;
  gap: 16px;
  overflow-y: auto;
  z-index: 1200;
}

.sidebar-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.brand {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  text-decoration: none;
  padding: 4px 8px;
}

.brand-logo {
  width: 150px;
  aspect-ratio: 1100 / 640;
  object-fit: cover;
  display: block;
}

.brand-caption {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--color-secondary);
  padding-left: 6px;
}

.sidebar-close {
  display: none;
  border: 0;
  background: rgba(255, 255, 255, 0.12);
  color: #ffffff;
  border-radius: 12px;
  width: 40px;
  height: 40px;
  place-items: center;
  cursor: pointer;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 18px;
  flex: 1;
}

.nav-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-title {
  margin: 0 0 4px;
  padding: 0 12px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.6);
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 46px;
  padding: 0 14px;
  border-radius: 14px;
  color: rgba(255, 255, 255, 0.86);
  font-weight: 600;
  text-decoration: none;
  transition: background-color 0.15s ease;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.1);
  text-decoration: none;
}

.nav-link.active {
  background: #ffffff;
  color: #5f2fd0;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.18);
}

:root[data-theme='dark'] .nav-link.active {
  background: rgba(185, 160, 245, 0.2);
  color: #ffffff;
}

.sidebar-bottom {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.16);
}

.sidebar-connection {
  color: rgba(255, 255, 255, 0.85);
  padding: 0 8px;
}

.profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 4px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.18);
  border: 2px solid rgba(255, 255, 255, 0.5);
  font-weight: 800;
  flex-shrink: 0;
}

.profile-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.profile-text strong {
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.profile-text span {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-actions {
  display: grid;
  grid-template-columns: 44px 1fr;
  gap: 8px;
}

.side-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 40px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.side-btn.icon-only {
  padding: 0;
}

.side-btn:hover {
  background: rgba(255, 255, 255, 0.18);
}

.main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  height: 100%;
}

.content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.topbar {
  display: none;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--sidebar-bg);
  color: #ffffff;
}

.topbar .btn-ghost {
  color: #ffffff;
}

.topbar-logo {
  width: 84px;
  aspect-ratio: 1100 / 640;
  object-fit: cover;
}

.topbar-connection {
  margin-left: auto;
  color: rgba(255, 255, 255, 0.9);
}

.backdrop {
  display: none;
}

@media (max-width: 960px) {
  .shell {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: fixed;
    inset: 0 auto 0 0;
    width: min(300px, 86vw);
    transform: translateX(-100%);
    transition: transform 0.25s ease;
  }

  .menu-open .sidebar {
    transform: translateX(0);
  }

  .sidebar-close {
    display: grid;
  }

  .menu-open .backdrop {
    display: block;
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    z-index: 1100;
  }

  .topbar {
    display: flex;
  }
}
</style>
