<script setup lang="ts">
import L from 'leaflet'
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import AsyncState from '@/components/AsyncState.vue'
import AppIcon from '@/components/AppIcon.vue'
import { driversApi } from '@/api/admin'
import { apiErrorMessage } from '@/api/http'
import { useLeafletMap } from '@/composables/useLeafletMap'
import { useRealtime } from '@/composables/useRealtime'
import { useNow } from '@/composables/useNow'
import { driverIcon, driverState, driverStateLabels, type DriverMarkerState } from '@/utils/mapIcons'
import { relativeTime } from '@/utils/format'
import { ACTIVE_TRIP_STATUSES, type AdminDriverLocation } from '@/types/models'

type Filter = 'all' | DriverMarkerState

const mapEl = ref<HTMLElement | null>(null)
const { map, fitTo } = useLeafletMap(mapEl)
const now = useNow()

const drivers = ref<Record<string, AdminDriverLocation>>({})
const loading = ref(true)
const error = ref<string | null>(null)
const selectedId = ref<string | null>(null)
const filter = ref<Filter>('all')
const search = ref('')

const markers = new Map<string, { marker: L.Marker; key: string }>()
let reloadTimer: number | undefined
let fittedOnce = false

const list = computed(() =>
  Object.values(drivers.value).sort((a, b) => {
    const order: Record<DriverMarkerState, number> = { busy: 0, free: 1, offline: 2 }
    const diff = order[stateOf(a)] - order[stateOf(b)]
    return diff !== 0 ? diff : `${a.givenName} ${a.familyName}`.localeCompare(`${b.givenName} ${b.familyName}`)
  })
)

const counts = computed(() => {
  const result: Record<DriverMarkerState, number> = { free: 0, busy: 0, offline: 0 }
  list.value.forEach((item) => result[stateOf(item)]++)
  return result
})

const visible = computed(() => {
  const term = search.value.trim().toLowerCase()
  return list.value.filter((item) => {
    if (filter.value !== 'all' && stateOf(item) !== filter.value) return false
    if (!term) return true
    return `${item.givenName} ${item.familyName} ${item.plate}`.toLowerCase().includes(term)
  })
})

function stateOf(item: AdminDriverLocation): DriverMarkerState {
  return driverState(item.online, item.tripId)
}

function isShown(item: AdminDriverLocation) {
  return filter.value === 'all' || stateOf(item) === filter.value
}

function syncMarker(item: AdminDriverLocation) {
  if (!map.value) return
  const state = stateOf(item)
  const selected = item.driverId === selectedId.value
  const key = `${state}|${Math.round(item.bearing ?? -1)}|${selected}`
  const existing = markers.get(item.driverId)
  if (!isShown(item)) {
    if (existing) {
      existing.marker.remove()
      markers.delete(item.driverId)
    }
    return
  }
  if (existing) {
    existing.marker.setLatLng([item.lat, item.lng])
    if (existing.key !== key) {
      existing.marker.setIcon(driverIcon(state, item.bearing, selected))
      existing.marker.setZIndexOffset(selected ? 1000 : 0)
      existing.key = key
    }
    existing.marker.setTooltipContent(tooltipFor(item))
    return
  }
  const marker = L.marker([item.lat, item.lng], {
    icon: driverIcon(state, item.bearing, selected),
    zIndexOffset: selected ? 1000 : 0,
    keyboard: false
  })
    .bindTooltip(tooltipFor(item), { direction: 'top', className: 'pin-label' })
    .on('click', () => select(item.driverId, false))
    .addTo(map.value)
  markers.set(item.driverId, { marker, key })
}

function tooltipFor(item: AdminDriverLocation) {
  return `${item.givenName} ${item.familyName} · ${item.plate} · ${driverStateLabels[stateOf(item)]}`
}

function syncAll() {
  const ids = new Set(Object.keys(drivers.value))
  for (const [id, entry] of markers) {
    if (!ids.has(id)) {
      entry.marker.remove()
      markers.delete(id)
    }
  }
  Object.values(drivers.value).forEach(syncMarker)
}

function upsert(id: string, patch: Partial<AdminDriverLocation>) {
  const current = drivers.value[id]
  if (!current) return false
  const next = { ...current, ...patch }
  drivers.value = { ...drivers.value, [id]: next }
  syncMarker(next)
  return true
}

function select(id: string, center = true) {
  const previous = selectedId.value
  selectedId.value = id
  const prevItem = previous ? drivers.value[previous] : undefined
  if (prevItem) syncMarker(prevItem)
  const item = drivers.value[id]
  if (!item) return
  syncMarker(item)
  if (center && map.value) map.value.flyTo([item.lat, item.lng], Math.max(map.value.getZoom(), 15), { duration: 0.6 })
  document.getElementById(`driver-${id}`)?.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
}

function fitAll() {
  fitTo(visible.value.map((item) => [item.lat, item.lng] as L.LatLngTuple))
}

async function load(showLoader = true) {
  if (showLoader) loading.value = true
  error.value = null
  try {
    const result = await driversApi.locations()
    drivers.value = Object.fromEntries(result.map((item) => [item.driverId, item]))
    syncAll()
    if (!fittedOnce && result.length > 0) {
      fittedOnce = true
      fitAll()
    }
  } catch (err) {
    error.value = apiErrorMessage(err, 'No se pudieron cargar las ubicaciones')
  } finally {
    loading.value = false
  }
}

function scheduleReload() {
  window.clearTimeout(reloadTimer)
  reloadTimer = window.setTimeout(() => load(false), 1500)
}

useRealtime('driver:location', (event) => {
  const updated = upsert(event.driverId, {
    lat: event.lat,
    lng: event.lng,
    bearing: event.bearing ?? null,
    speed: event.speed ?? null,
    at: event.at,
    tripId: event.tripId ?? null
  })
  if (!updated) scheduleReload()
})

useRealtime('driver:availability', (event) => {
  if (!upsert(event.driverId, { online: event.online })) scheduleReload()
})

useRealtime('trip:updated', (trip) => {
  if (!trip.driver) return
  const active = ACTIVE_TRIP_STATUSES.includes(trip.status)
  const current = drivers.value[trip.driver.id]
  if (!current) return
  if (active && current.tripId !== trip.id) upsert(trip.driver.id, { tripId: trip.id })
  if (!active && current.tripId === trip.id) upsert(trip.driver.id, { tripId: null })
})

watch(filter, syncAll)
watch(map, (value) => {
  if (value) syncAll()
})

onMounted(() => load())
onBeforeUnmount(() => window.clearTimeout(reloadTimer))
</script>

<template>
  <div class="page-full">
    <PageHeader title="Mapa de conductores" subtitle="Ubicación de los conductores actualizada en tiempo real.">
      <template #actions>
        <div class="legend">
          <span><i class="dot free" /> Libre ({{ counts.free }})</span>
          <span><i class="dot busy" /> Ocupado ({{ counts.busy }})</span>
          <span><i class="dot offline" /> Fuera de labores ({{ counts.offline }})</span>
        </div>
      </template>
    </PageHeader>

    <div class="map-layout">
      <section class="card map-card">
        <div ref="mapEl" class="map-canvas" aria-label="Mapa de conductores" />
        <div class="map-overlay">
          <button class="btn btn-secondary btn-sm" type="button" @click="fitAll">
            <AppIcon name="crosshair" :size="16" />
            Ver todos
          </button>
          <button class="btn btn-secondary btn-sm" type="button" :disabled="loading" @click="load(false)">
            <AppIcon name="refresh" :size="16" />
            Actualizar
          </button>
        </div>
      </section>

      <aside class="card side-panel">
        <div class="side-panel-header stack">
          <div class="row">
            <h2 class="card-title" style="margin: 0">Conductores</h2>
            <span class="spacer" />
            <span class="muted">{{ visible.length }} de {{ list.length }}</span>
          </div>
          <div class="search-box" style="max-width: none">
            <AppIcon name="search" :size="18" />
            <input v-model="search" class="input" type="search" placeholder="Nombre o placa" aria-label="Buscar conductor" />
          </div>
          <div class="chip-group" role="tablist" aria-label="Filtrar por estado">
            <button class="chip" :class="{ active: filter === 'all' }" type="button" @click="filter = 'all'">Todos</button>
            <button class="chip" :class="{ active: filter === 'free' }" type="button" @click="filter = 'free'">Libres</button>
            <button class="chip" :class="{ active: filter === 'busy' }" type="button" @click="filter = 'busy'">Ocupados</button>
            <button class="chip" :class="{ active: filter === 'offline' }" type="button" @click="filter = 'offline'">Fuera</button>
          </div>
        </div>
        <div class="side-panel-body">
          <AsyncState
            :loading="loading"
            :error="error"
            :empty="visible.length === 0"
            :empty-text="list.length === 0 ? 'Ningún conductor ha reportado su ubicación todavía' : 'Ningún conductor coincide con el filtro'"
            @retry="load()"
          >
            <button
              v-for="item in visible"
              :id="`driver-${item.driverId}`"
              :key="item.driverId"
              class="list-item"
              :class="{ selected: item.driverId === selectedId }"
              type="button"
              @click="select(item.driverId)"
            >
              <i class="dot" :class="stateOf(item)" />
              <div class="item-text">
                <div class="cell-main">{{ item.givenName }} {{ item.familyName }}</div>
                <div class="cell-sub">
                  {{ item.plate }} · {{ driverStateLabels[stateOf(item)] }}
                  <template v-if="item.speed !== null && item.speed > 0"> · {{ Math.round(item.speed * 3.6) }} km/h</template>
                </div>
                <RouterLink v-if="item.tripId" :to="`/trips/${item.tripId}`" class="trip-link" @click.stop>
                  Ver viaje {{ item.tripId.slice(0, 8) }}
                </RouterLink>
              </div>
              <span class="cell-sub time">{{ relativeTime(item.at, now) }}</span>
            </button>
          </AsyncState>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.map-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 500;
  display: flex;
  gap: 8px;
}

.map-overlay .btn {
  box-shadow: var(--shadow-md);
}

.item-text {
  flex: 1;
  min-width: 0;
}

.time {
  align-self: flex-start;
  white-space: nowrap;
}

.trip-link {
  font-size: 12px;
}

.chip-group {
  align-self: flex-start;
}
</style>
