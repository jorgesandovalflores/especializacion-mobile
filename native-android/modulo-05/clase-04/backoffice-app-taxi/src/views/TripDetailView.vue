<script setup lang="ts">
import L from 'leaflet'
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import AsyncState from '@/components/AsyncState.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import AppIcon from '@/components/AppIcon.vue'
import { tripsApi } from '@/api/admin'
import { apiErrorMessage } from '@/api/http'
import { useLeafletMap } from '@/composables/useLeafletMap'
import { useRealtime } from '@/composables/useRealtime'
import { useNow } from '@/composables/useNow'
import { decodePolyline } from '@/utils/polyline'
import { driverIcon, placeIcon, ROUTE_COLOR, TRACK_COLOR } from '@/utils/mapIcons'
import {
  formatDateTime,
  formatDistance,
  formatDuration,
  formatMoney,
  formatPhone,
  formatTime,
  fullName,
  relativeTime
} from '@/utils/format'
import { ACTIVE_TRIP_STATUSES, type DriverLocation, type TrackPoint, type TripDetailDto } from '@/types/models'

const props = defineProps<{ id: string }>()

const mapEl = ref<HTMLElement | null>(null)
const { map, fitTo } = useLeafletMap(mapEl, 13)
const now = useNow()

const trip = ref<TripDetailDto | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const live = ref<DriverLocation | null>(null)

let routeLayer: L.Polyline | null = null
let trackLayer: L.Polyline | null = null
let originMarker: L.Marker | null = null
let destinationMarker: L.Marker | null = null
let driverMarker: L.Marker | null = null
let reloadTimer: number | undefined

const isActive = computed(() => !!trip.value && ACTIVE_TRIP_STATUSES.includes(trip.value.status))

const routePoints = computed<L.LatLngTuple[]>(() => decodePolyline(trip.value?.polyline))

const trackPoints = computed<L.LatLngTuple[]>(() =>
  (trip.value?.track ?? []).map((point) => [point.lat, point.lng] as L.LatLngTuple)
)

const trackDistance = computed(() => {
  const points = trackPoints.value
  let meters = 0
  for (let i = 1; i < points.length; i++) {
    const a = points[i - 1]
    const b = points[i]
    if (a && b) meters += L.latLng(a).distanceTo(L.latLng(b))
  }
  return meters
})

interface TimelineStep {
  label: string
  at: string | null
  done: boolean
  tone: 'done' | 'current' | 'pending' | 'cancelled'
  note?: string
}

const timeline = computed<TimelineStep[]>(() => {
  const value = trip.value
  if (!value) return []
  const steps: { label: string; at: string | null }[] = [
    { label: 'Solicitado', at: value.requestedAt },
    { label: 'Oferta aceptada', at: value.acceptedAt },
    { label: 'Conductor llegó', at: value.arrivedAt },
    { label: 'Viaje iniciado', at: value.startedAt },
    { label: 'Completado', at: value.completedAt }
  ]
  if (value.status === 'CANCELLED') {
    const reached = steps.filter((step) => step.at)
    return [
      ...reached.map((step) => ({ ...step, done: true, tone: 'done' as const })),
      {
        label: 'Cancelado',
        at: value.cancelledAt,
        done: true,
        tone: 'cancelled' as const,
        note: value.cancelledBy === 'DRIVER' ? 'Por el conductor' : value.cancelledBy === 'PASSENGER' ? 'Por el pasajero' : undefined
      }
    ]
  }
  const lastDone = steps.reduce((acc, step, index) => (step.at ? index : acc), 0)
  return steps.map((step, index) => ({
    ...step,
    done: !!step.at,
    tone: index === lastDone && value.status !== 'COMPLETED' ? 'current' : step.at ? 'done' : 'pending'
  }))
})

const sortedOffers = computed(() =>
  [...(trip.value?.offers ?? [])].sort((a, b) => a.createdAt.localeCompare(b.createdAt))
)

function drawStatic() {
  if (!map.value || !trip.value) return
  const value = trip.value
  routeLayer?.remove()
  trackLayer?.remove()
  originMarker?.remove()
  destinationMarker?.remove()
  const planned = routePoints.value.length > 1
    ? routePoints.value
    : ([
        [value.origin.lat, value.origin.lng],
        [value.destination.lat, value.destination.lng]
      ] as L.LatLngTuple[])
  routeLayer = L.polyline(planned, {
    color: ROUTE_COLOR,
    weight: 6,
    opacity: 0.75,
    dashArray: routePoints.value.length > 1 ? undefined : '8 8'
  })
    .bindTooltip('Ruta planificada', { sticky: true, className: 'pin-label' })
    .addTo(map.value)
  trackLayer = L.polyline(trackPoints.value, { color: TRACK_COLOR, weight: 4, opacity: 0.95, dashArray: '2 8', lineCap: 'round' })
    .bindTooltip('Recorrido real', { sticky: true, className: 'pin-label' })
    .addTo(map.value)
  originMarker = L.marker([value.origin.lat, value.origin.lng], { icon: placeIcon('origin') })
    .bindTooltip(value.origin.address || 'Origen', { direction: 'top', className: 'pin-label' })
    .addTo(map.value)
  destinationMarker = L.marker([value.destination.lat, value.destination.lng], { icon: placeIcon('destination') })
    .bindTooltip(value.destination.address || 'Destino', { direction: 'top', className: 'pin-label' })
    .addTo(map.value)
  drawDriver()
}

function drawDriver() {
  if (!map.value) return
  const position = live.value
  if (!position || !isActive.value) {
    driverMarker?.remove()
    driverMarker = null
    return
  }
  const icon = driverIcon('busy', position.bearing, true)
  if (driverMarker) {
    driverMarker.setLatLng([position.lat, position.lng])
    driverMarker.setIcon(icon)
  } else {
    driverMarker = L.marker([position.lat, position.lng], { icon, zIndexOffset: 1000 })
      .bindTooltip(`${fullName(trip.value?.driver)} · en vivo`, { direction: 'top', className: 'pin-label' })
      .addTo(map.value)
  }
}

function fitTrip() {
  const value = trip.value
  if (!value) return
  const points: L.LatLngTuple[] = [
    [value.origin.lat, value.origin.lng],
    [value.destination.lat, value.destination.lng],
    ...routePoints.value,
    ...trackPoints.value
  ]
  if (live.value && isActive.value) points.push([live.value.lat, live.value.lng])
  fitTo(points)
}

function seedLive(value: TripDetailDto) {
  const last = value.track[value.track.length - 1]
  if (!live.value && last && value.driver) {
    live.value = {
      driverId: value.driver.id,
      tripId: value.id,
      lat: last.lat,
      lng: last.lng,
      bearing: null,
      speed: null,
      at: last.at
    }
  }
}

async function load(showLoader = true) {
  if (showLoader) loading.value = true
  error.value = null
  try {
    const value = await tripsApi.get(props.id)
    const first = !trip.value || trip.value.id !== value.id
    trip.value = value
    seedLive(value)
    drawStatic()
    if (first) fitTrip()
  } catch (err) {
    error.value = apiErrorMessage(err, 'No se pudo cargar el viaje')
  } finally {
    loading.value = false
  }
}

useRealtime('driver:location', (event) => {
  const value = trip.value
  if (!value || !isActive.value) return
  const matches = event.tripId === value.id || (!!value.driver && event.driverId === value.driver.id)
  if (!matches) return
  live.value = event
  if (value.status === 'IN_PROGRESS' || value.status === 'ARRIVED' || value.status === 'ACCEPTED') {
    const point: TrackPoint = { lat: event.lat, lng: event.lng, at: event.at }
    trip.value = { ...value, track: [...value.track, point] }
    trackLayer?.addLatLng([event.lat, event.lng])
  }
  drawDriver()
})

useRealtime('trip:updated', (updated) => {
  if (updated.id !== props.id || !trip.value) return
  trip.value = { ...trip.value, ...updated }
  drawDriver()
  window.clearTimeout(reloadTimer)
  reloadTimer = window.setTimeout(() => load(false), 600)
})

watch(map, (value) => {
  if (value && trip.value) {
    drawStatic()
    fitTrip()
  }
})

watch(
  () => props.id,
  () => {
    trip.value = null
    live.value = null
    load()
  }
)

onMounted(() => load())
onBeforeUnmount(() => window.clearTimeout(reloadTimer))
</script>

<template>
  <div class="page">
    <PageHeader :title="trip ? `Viaje ${trip.id.slice(0, 8)}` : 'Detalle del viaje'" :subtitle="trip ? `Solicitado el ${formatDateTime(trip.requestedAt)}` : undefined">
      <template #before>
        <RouterLink to="/trips" class="back-link">
          <AppIcon name="arrow-left" :size="16" />
          Volver a viajes
        </RouterLink>
      </template>
      <template #actions>
        <span v-if="isActive" class="live-pill"><i /> En vivo</span>
        <StatusBadge v-if="trip" type="trip" :value="trip.status" />
        <button class="btn btn-secondary btn-sm" type="button" :disabled="loading" @click="load(false)">
          <AppIcon name="refresh" :size="16" />
          Actualizar
        </button>
      </template>
    </PageHeader>

    <div class="detail-grid">
      <section class="card map-card detail-map">
        <div ref="mapEl" class="map-canvas" aria-label="Mapa del viaje" />
        <div v-if="trip" class="map-legend card">
          <span><i class="line planned" /> Ruta planificada</span>
          <span><i class="line track" /> Recorrido real ({{ trip.track.length }} puntos)</span>
          <span v-if="isActive"><i class="dot busy" /> Conductor en vivo</span>
        </div>
        <button v-if="trip" class="btn btn-secondary btn-sm fit-btn" type="button" @click="fitTrip">
          <AppIcon name="crosshair" :size="16" />
          Centrar
        </button>
        <div v-if="loading || error" class="map-state">
          <AsyncState :loading="loading" :error="error" @retry="load()" />
        </div>
      </section>

      <aside v-if="trip" class="stack">
        <section class="card card-body">
          <h2 class="card-title">Línea de tiempo</h2>
          <ol class="timeline">
            <li v-for="step in timeline" :key="step.label" :class="step.tone">
              <span class="marker" />
              <div>
                <div class="step-label">{{ step.label }}</div>
                <div class="cell-sub">
                  {{ step.at ? formatTime(step.at) : 'Pendiente' }}
                  <template v-if="step.note"> · {{ step.note }}</template>
                </div>
              </div>
            </li>
          </ol>
          <p v-if="live && isActive" class="cell-sub live-note">Última señal del conductor {{ relativeTime(live.at, now) }}</p>
        </section>

        <section class="card card-body">
          <h2 class="card-title">Tarifa y recorrido</h2>
          <div class="stat-grid">
            <div class="stat">
              <div class="stat-label">Estimada</div>
              <div class="stat-value">{{ formatMoney(trip.estimatedFare, trip.currency) }}</div>
            </div>
            <div class="stat">
              <div class="stat-label">Final</div>
              <div class="stat-value accent">{{ formatMoney(trip.finalFare, trip.currency) }}</div>
            </div>
            <div class="stat">
              <div class="stat-label">Distancia planificada</div>
              <div class="stat-value small">{{ formatDistance(trip.distanceMeters) }} · {{ formatDuration(trip.durationSeconds) }}</div>
            </div>
            <div class="stat">
              <div class="stat-label">Recorrido real</div>
              <div class="stat-value small">{{ trackPoints.length > 1 ? formatDistance(trackDistance) : '—' }}</div>
            </div>
          </div>
        </section>
      </aside>
    </div>

    <div v-if="trip" class="info-grid">
      <section class="card card-body">
        <h2 class="card-title">Recorrido</h2>
        <div class="places">
          <div><i class="dot" style="background: #2e7d32" /><div><div class="cell-sub">Origen</div>{{ trip.origin.address || `${trip.origin.lat}, ${trip.origin.lng}` }}</div></div>
          <div><i class="dot" style="background: #c62828" /><div><div class="cell-sub">Destino</div>{{ trip.destination.address || `${trip.destination.lat}, ${trip.destination.lng}` }}</div></div>
        </div>
      </section>

      <section class="card card-body">
        <h2 class="card-title">Pasajero</h2>
        <dl class="data">
          <dt>Nombre</dt>
          <dd>{{ fullName(trip.passenger) }}</dd>
          <dt>Teléfono</dt>
          <dd>{{ formatPhone(trip.passenger.phone) }}</dd>
          <dt>Calificó al conductor</dt>
          <dd>{{ trip.ratingByPassenger ? `★ ${trip.ratingByPassenger}` : '—' }}</dd>
        </dl>
      </section>

      <section class="card card-body">
        <h2 class="card-title">Conductor</h2>
        <dl v-if="trip.driver" class="data">
          <dt>Nombre</dt>
          <dd>{{ fullName(trip.driver) }}</dd>
          <dt>Teléfono</dt>
          <dd>{{ formatPhone(trip.driver.phone) }}</dd>
          <dt>Vehículo</dt>
          <dd>{{ trip.driver.vehicle?.plate }} · {{ trip.driver.vehicle?.brand }} {{ trip.driver.vehicle?.model }} · {{ trip.driver.vehicle?.color }}</dd>
          <dt>Calificación</dt>
          <dd>{{ trip.driver.rating ? `★ ${trip.driver.rating.toFixed(1)}` : '—' }}</dd>
          <dt>Calificó al pasajero</dt>
          <dd>{{ trip.ratingByDriver ? `★ ${trip.ratingByDriver}` : '—' }}</dd>
        </dl>
        <p v-else class="muted">Aún no hay conductor asignado.</p>
      </section>
    </div>

    <section v-if="trip" class="card offers">
      <div class="toolbar">
        <h2 class="card-title" style="margin: 0">Ofertas</h2>
        <span class="badge">{{ sortedOffers.length }}</span>
      </div>
      <AsyncState :loading="false" :error="null" :empty="sortedOffers.length === 0" empty-text="Este viaje no recibió ofertas">
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Hora</th>
                <th>Conductor</th>
                <th>Vehículo</th>
                <th class="num">Precio</th>
                <th class="num hide-sm">Llegada estimada</th>
                <th class="num hide-sm">Distancia al origen</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="offer in sortedOffers" :key="offer.id" :class="{ winner: offer.status === 'ACCEPTED' }">
                <td>{{ formatTime(offer.createdAt) }}</td>
                <td>
                  <div class="cell-main">{{ fullName(offer.driver) }}</div>
                  <div class="cell-sub">{{ offer.driver.rating ? `★ ${offer.driver.rating.toFixed(1)}` : 'Sin calificación' }}</div>
                </td>
                <td>{{ offer.driver.vehicle?.plate }} <span class="cell-sub">{{ offer.driver.vehicle?.brand }} {{ offer.driver.vehicle?.model }}</span></td>
                <td class="num cell-main">{{ formatMoney(offer.price, offer.currency) }}</td>
                <td class="num hide-sm">{{ formatDuration(offer.etaSeconds) }}</td>
                <td class="num hide-sm">{{ formatDistance(offer.distanceToPickupMeters) }}</td>
                <td><StatusBadge type="offer" :value="offer.status" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </AsyncState>
    </section>
  </div>
</template>

<style scoped>
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  margin-bottom: 8px;
}

.live-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--color-error-soft);
  color: var(--color-error);
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.live-pill i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
  animation: blink 1.2s infinite;
}

@keyframes blink {
  50% {
    opacity: 0.25;
  }
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 16px;
  align-items: stretch;
}

.detail-map {
  min-height: 520px;
}

.map-state {
  position: absolute;
  inset: 0;
  z-index: 600;
  display: grid;
  place-items: center;
  background: color-mix(in srgb, var(--color-surface) 85%, transparent);
}

.map-legend {
  position: absolute;
  left: 12px;
  bottom: 12px;
  z-index: 500;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 10px 14px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: var(--shadow-md);
}

.map-legend span {
  display: flex;
  align-items: center;
  gap: 8px;
}

.line {
  width: 24px;
  height: 0;
  border-top: 5px solid #7843e6;
  border-radius: 3px;
}

.line.track {
  border-top: 4px dotted #00b8d4;
}

.fit-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 500;
  box-shadow: var(--shadow-md);
}

.timeline {
  list-style: none;
  margin: 0;
  padding: 0;
}

.timeline li {
  position: relative;
  display: flex;
  gap: 14px;
  padding-bottom: 16px;
}

.timeline li:last-child {
  padding-bottom: 0;
}

.timeline li::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 20px;
  bottom: 0;
  width: 2px;
  background: var(--color-outline-soft);
}

.timeline li:last-child::before {
  display: none;
}

.timeline li.done::before {
  background: var(--color-primary);
}

.timeline .marker {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 3px solid var(--color-outline);
  background: var(--color-surface);
  flex-shrink: 0;
  margin-top: 1px;
}

.timeline .done .marker {
  border-color: var(--color-primary);
  background: var(--color-primary);
}

.timeline .current .marker {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 5px var(--color-primary-soft);
}

.timeline .cancelled .marker {
  border-color: var(--color-error);
  background: var(--color-error);
}

.timeline .pending .step-label {
  color: var(--color-on-surface-variant);
}

.step-label {
  font-weight: 700;
}

.live-note {
  margin: 14px 0 0;
}

.stat-grid {
  grid-template-columns: 1fr 1fr;
}

.stat-value.accent {
  color: var(--color-primary);
}

.stat-value.small {
  font-size: 15px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.places {
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-weight: 600;
}

.places > div {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.places .dot {
  margin-top: 4px;
}

.data {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 8px 16px;
  margin: 0;
  font-size: 14px;
}

.data dt {
  color: var(--color-on-surface-variant);
}

.data dd {
  margin: 0;
  font-weight: 600;
}

.offers {
  margin-top: 16px;
}

.winner {
  background: var(--color-success-soft);
}

@media (max-width: 1100px) {
  .detail-grid,
  .info-grid {
    grid-template-columns: 1fr;
  }

  .detail-map {
    min-height: 420px;
  }
}
</style>
