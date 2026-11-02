import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppLayout from '@/layouts/AppLayout.vue'

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    title?: string
  }
}

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { public: true, title: 'Ingresar' }
    },
    {
      path: '/',
      component: AppLayout,
      children: [
        { path: '', redirect: { name: 'drivers-map' } },
        {
          path: 'map/drivers',
          name: 'drivers-map',
          component: () => import('@/views/DriversMapView.vue'),
          meta: { title: 'Mapa de conductores' }
        },
        {
          path: 'map/trips',
          name: 'trips-map',
          component: () => import('@/views/TripsMapView.vue'),
          meta: { title: 'Mapa de viajes' }
        },
        {
          path: 'trips',
          name: 'trips',
          component: () => import('@/views/TripsView.vue'),
          meta: { title: 'Viajes' }
        },
        {
          path: 'trips/:id',
          name: 'trip-detail',
          component: () => import('@/views/TripDetailView.vue'),
          props: true,
          meta: { title: 'Detalle del viaje' }
        },
        {
          path: 'drivers',
          name: 'drivers',
          component: () => import('@/views/DriversView.vue'),
          meta: { title: 'Conductores' }
        },
        {
          path: 'passengers',
          name: 'passengers',
          component: () => import('@/views/PassengersView.vue'),
          meta: { title: 'Pasajeros' }
        },
        {
          path: 'admins',
          name: 'admins',
          component: () => import('@/views/AdminsView.vue'),
          meta: { title: 'Administradores' }
        },
        {
          path: 'tariff',
          name: 'tariff',
          component: () => import('@/views/TariffView.vue'),
          meta: { title: 'Tarifa' }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
      meta: { public: true, title: 'No encontrado' }
    }
  ]
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.isAuthenticated) {
    return { name: 'login', query: to.fullPath !== '/' ? { redirect: to.fullPath } : {} }
  }
  if (to.name === 'login' && auth.isAuthenticated) {
    return { name: 'drivers-map' }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · App Taxi Backoffice` : 'App Taxi Backoffice'
})
