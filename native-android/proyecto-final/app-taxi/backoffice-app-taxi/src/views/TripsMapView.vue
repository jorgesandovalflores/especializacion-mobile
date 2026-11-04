<script setup lang="ts">
import L from 'leaflet'
import { computed, onMounted, ref, watch } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import AsyncState from '@/components/AsyncState.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import AppIcon from '@/components/AppIcon.vue'
import { driversApi, tripsApi } from '@/api/admin'
import { apiErrorMessage } from '@/api/http'
import { useLeafletMap } from '@/composables/useLeafletMap'
import { useRealtime } from '@/composables/useRealtime'
import { useNow } from '@/composables/useNow'
import { decodePolyline } from '@/utils/polyline'
import { driverIcon, placeIcon, ROUTE_COLOR } from '@/utils/mapIcons'
import { formatDistance, formatMoney, fullName, relativeTime, tripStatusLabels } from '@/utils/format'
import { ACTIVE_TRIP_STATUSES, type DriverLocation, type TripDto, type TripStatus } from '@/types/models'

interface TripLayers {
  group: L.LayerGroup
  route: L.Polyline | null
  driver: L.Marker | null
}

const mapEl = ref<HTMLElement | null>(null)
const { map, fitTo } = useLeafletMap(mapEl)
const now = useNow()

const trips = ref<Record<string, TripDto>>({})
const positions = ref<Record<string, DriverLocation>>({})
const loading = ref(true)
const error = ref<string | null>(null)
const selectedId = ref<string | null>(null)

const layers = new Map<string, TripLayers>()
let fittedOnce = false

const statusOrder: Record<TripStatus, number> = {
  IN_PROGRESS: 0,
  ARRIVED: 1,
  ACCEPTED: 2,
  REQUESTED: 3,
  COMPLETED: 4,
  CANCELLED: 5
}

const list = computed(() =>
  Object.values(trips.value).sort(
    (a, b) => statusOrder[a.status] - statusOrder[b.status] || b.requestedAt.localeCompare(a.requestedAt)
  )
)

const counts = computed(() => {
  const result: Partial<Record<TripStatus, number>> = {}
  list.value.forEach((trip) => {
    result[trip.status] = (result[trip.status] ?? 0) + 1
  })
  return result
})

function routePoints(trip: TripDto): L.LatLngTuple[] {
  const decoded = decodePolyline(trip.polyline)
  if (decoded.length > 1) return decoded
  return [
    [trip.origin.lat, trip.origin.lng],
    [trip.destination.lat, trip.destination.lng]
  ]
}

function routeStyle(tripId: string): L.PolylineOptions {
  const selected = selectedId.value === tripId
  const dimmed = selectedId.value !== null && !selected
  return {
    color: ROUTE_COLOR,
    weight: selected ? 6 : 4,
    opacity: dimmed ? 0.25 : selected ? 0.95 : 0.6,
    dashArray: trips.value[tripId]?.status === 'REQUESTED' ? '8 8' : undefined
  }
}

function driverPositionFor(trip: TripDto): DriverLocation | null {
  if (!trip.driver) return null
  return positions.value[trip.driver.id] ?? null
}

function syncDriverMarker(trip: TripDto) {
  const entry = layers.get(trip.id)
  if (!entry) return
  const position = driverPositionFor(trip)
  if (!position) {
    entry.driver?.remove()
    entry.driver = null
    return
  }
  const icon = driverIcon('busy', position.bearing, selectedId.value === trip.id)
  if (entry.driver) {
    entry.driver.setLatLng([position.lat, position.lng])
    entry.driver.setIcon(icon)
  } else {
    entry.driver = L.marker([position.lat, position.lng], { icon, zIndexOffset: 800 })
      .bindTooltip(`${fullName(trip.driver)} · ${trip.driver?.vehicle?.plate ?? ''}`, {
        direction: 'top',
        className: 'pin-label'
      })
      .on('click', () => select(trip.id, false))
    entry.group.addLayer(entry.driver)
  }
}

function drawTrip(trip: TripDto) {
  if (!map.value) return
  removeTrip(trip.id)
  const group = L.layerGroup().addTo(map.value)
  const route = L.polyline(routePoints(trip), routeStyle(trip.id)).on('click', () => select(trip.id, false))
  group.addLayer(route)
  group.addLayer(
    L.marker([trip.origin.lat, trip.origin.lng], { icon: placeIcon('origin') })
      .bindTooltip(trip.origin.address || 'Origen', { direction: 'top', className: 'pin-label' })
      .on('click', () => select(trip.id, false))
  )
  group.addLayer(
    L.marker([trip.destination.lat, trip.destination.lng], { icon: placeIcon('destination') })
      .bindTooltip(trip.destination.address || 'Destino', { direction: 'top', className: 'pin-label' })
      .on('click', () => select(trip.id, false))
  )
  layers.set(trip.id, { group, route, driver: null })
  syncDriverMarker(trip)
}

function removeTrip(id: string) {
  const entry = layers.get(id)
  if (!entry) return
  entry.group.remove()
  layers.delete(id)
}

function restyle() {
  for (const [id, entry] of layers) {
    entry.route?.setStyle(routeStyle(id))
    if (id === selectedId.value) entry.route?.bringToFront()
    const trip = trips.value[id]
    if (trip) syncDriverMarker(trip)
  }
}

function drawAll() {
  for (const id of [...layers.keys()]) removeTrip(id)
  Object.values(trips.value).forEach(drawTrip)
}

function tripBounds(trip: TripDto): L.LatLngTuple[] {
  const points = routePoints(trip)
  const position = driverPositionFor(trip)
  if (position) points.push([position.lat, position.lng])
  return points
}

function select(id: string, fit = true) {
  selectedId.value = selectedId.value === id && fit ? null : id
  restyle()
  const trip = trips.value[id]
  if (fit && trip && selectedId.value) fitTo(tripBounds(trip))
  document.getElementById(`trip-${id}`)?.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
}

function fitAll() {
  fitTo(Object.values(trips.value).flatMap(tripBounds))
}

async function load(showLoader = true) {
  if (showLoader) loading.value = true
  error.value = null
  try {
    const [active, locations] = await Promise.all([tripsApi.active(), driversApi.locations().catch(() => [])])
    trips.value = Object.fromEntries(active.map((trip) => [trip.id, trip]))
    const driverIds = new Set(active.map((trip) => trip.driver?.id).filter(Boolean))
    positions.value = Object.fromEntries(
      locations.filter((item) => driverIds.has(item.driverId)).map((item) => [item.driverId, item])
    )
    if (selectedId.value && !trips.value[selectedId.value]) selectedId.value = null
    drawAll()
    if (!fittedOnce && active.length > 0) {
      fittedOnce = true
      fitAll()
    }
  } catch (err) {
    error.value = apiErrorMessage(err, 'No se pudieron cargar los viajes activos')
  } finally {
    loading.value = false
  }
}

useRealtime('trip:updated', (trip) => {
  if (ACTIVE_TRIP_STATUSES.includes(trip.status)) {
    const previous = trips.value[trip.id]
    trips.value = { ...trips.value, [trip.id]: trip }
    if (!previous || previous.polyline !== trip.polyline || previous.status !== trip.status) drawTrip(trip)
    else syncDriverMarker(trip)
    restyle()
    return
  }
  if (!trips.value[trip.id]) return
  const next = { ...trips.value }
  delete next[trip.id]
  trips.value = next
  removeTrip(trip.id)
  if (selectedId.value === trip.id) {
    selectedId.value = null
    restyle()
  }
})

useRealtime('driver:location', (event) => {
  const trip = Object.values(trips.value).find(
    (item) => item.driver?.id === event.driverId || (event.tripId !== null && item.id === event.tripId)
  )
  if (!trip) return
  positions.value = { ...positions.value, [event.driverId]: event }
  syncDriverMarker(trip)
})

watch(map, (value) => {
  if (value) drawAll()
})

onMounted(() => load())
</script>

<template>
  <div class="page-full">
    <PageHeader title="Mapa de viajes" subtitle="Viajes activos con su ruta y la posición del conductor en vivo.">
      <template #actions>
        <div class="legend">
          <span><i class="dot" style="background: #2e7d32" /> Origen</span>
          <span><i class="dot" style="background: #c62828" /> Destino</span>
          <span><i class="dot busy" /> Conductor</span>
          <span><i class="route-sample" /> Ruta</span>
        </div>
      </template>
    </PageHeader>

    <div class="map-layout">
      <section class="card map-card">
        <div ref="mapEl" class="map-canvas" aria-label="Mapa de viajes activos" />
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
            <h2 class="card-title" style="margin: 0">Viajes activos</h2>
            <span class="spacer" />
            <span class="badge primary">{{ list.length }}</span>
          </div>
          <div class="summary">
            <span v-for="status in ACTIVE_TRIP_STATUSES" :key="status" class="summary-item">
              <strong>{{ counts[status] ?? 0 }}</strong> {{ tripStatusLabels[status] }}
            </span>
          </div>
        </div>
        <div class="side-panel-body">
          <AsyncState
            :loading="loading"
            :error="error"
            :empty="list.length === 0"
            empty-text="No hay viajes activos en este momento"
            @retry="load()"
          >
            <div
              v-for="trip in list"
              :id="`trip-${trip.id}`"
              :key="trip.id"
              class="list-item trip-item"
              :class="{ selected: trip.id === selectedId }"
              role="button"
              tabindex="0"
              @click="select(trip.id)"
              @keydown.enter="select(trip.id)"
            >
              <div class="trip-head">
                <StatusBadge type="trip" :value="trip.status" />
                <span class="cell-sub">{{ relativeTime(trip.requestedAt, now) }}</span>
              </div>
              <div class="places">
                <div><i class="dot" style="background: #2e7d32" /> {{ trip.origin.address || `${trip.origin.lat.toFixed(4)}, ${trip.origin.lng.toFixed(4)}` }}</div>
                <div><i class="dot" style="background: #c62828" /> {{ trip.destination.address || `${trip.destination.lat.toFixed(4)}, ${trip.destination.lng.toFixed(4)}` }}</div>
              </div>
              <div class="trip-meta">
                <span>Pasajero: <strong>{{ fullName(trip.passenger) }}</strong></span>
                <span v-if="trip.driver">
                  Conductor: <strong>{{ fullName(trip.driver) }}</strong> · {{ trip.driver.vehicle?.plate }}
                </span>
                <span v-else class="muted">Esperando ofertas de conductores</span>
                <span>
                  {{ formatDistance(trip.distanceMeters) }} ·
                  {{ formatMoney(trip.finalFare ?? trip.estimatedFare, trip.currency) }}
                  <template v-if="trip.driver && positions[trip.driver.id]">
                    · señal {{ relativeTime(positions[trip.driver.id]?.at, now) }}
                  </template>
                </span>
              </div>
              <RouterLink :to="`/trips/${trip.id}`" class="detail-link" @click.stop>
                Ver detalle del viaje
                <AppIcon name="chevron-right" :size="16" />
              </RouterLink>
            </div>
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

.route-sample {
  width: 22px;
  height: 4px;
  border-radius: 2px;
  background: #7843e6;
}

.summary {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 14px;
  font-size: 12px;
  color: var(--color-on-surface-variant);
}

.summary strong {
  color: var(--color-on-surface);
}

.trip-item {
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
  margin-bottom: 4px;
}

.trip-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.places {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
}

.places div {
  display: flex;
  align-items: center;
  gap: 8px;
}

.places .dot {
  width: 10px;
  height: 10px;
}

.trip-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
  color: var(--color-on-surface-variant);
}

.trip-meta strong {
  color: var(--color-on-surface);
}

.detail-link {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 13px;
  align-self: flex-start;
}
</style>
