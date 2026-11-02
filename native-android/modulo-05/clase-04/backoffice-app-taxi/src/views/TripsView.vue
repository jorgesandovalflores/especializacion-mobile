<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import AsyncState from '@/components/AsyncState.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import AppIcon from '@/components/AppIcon.vue'
import { tripsApi } from '@/api/admin'
import { usePagedQuery } from '@/composables/usePagedQuery'
import { useRealtime } from '@/composables/useRealtime'
import { formatDateTime, formatDistance, formatMoney, fullName, tripStatusLabels } from '@/utils/format'
import { ALL_TRIP_STATUSES, type TripStatus } from '@/types/models'

const route = useRoute()
const router = useRouter()
const initialStatus = typeof route.query.status === 'string' && ALL_TRIP_STATUSES.includes(route.query.status as TripStatus)
  ? route.query.status
  : ''
const status = ref<string>(initialStatus)

const { items, total, page, size, loading, error, load, goTo } = usePagedQuery(tripsApi.list, {
  errorMessage: 'No se pudo cargar el historial de viajes',
  filter: status
})

function setStatus(value: string) {
  status.value = value
  router.replace({ query: value ? { status: value } : {} })
}

useRealtime('trip:updated', (trip) => {
  if (!items.value.some((item) => item.id === trip.id)) return
  items.value = items.value.map((item) => (item.id === trip.id ? trip : item))
})

function shortAddress(point: { lat: number; lng: number; address?: string | null }) {
  return point.address || `${point.lat.toFixed(4)}, ${point.lng.toFixed(4)}`
}

onMounted(load)
</script>

<template>
  <div class="page">
    <PageHeader title="Viajes" subtitle="Historial de todos los viajes solicitados.">
      <template #actions>
        <RouterLink to="/map/trips" class="btn btn-secondary">
          <AppIcon name="route" />
          Ver activos en el mapa
        </RouterLink>
      </template>
    </PageHeader>

    <section class="card">
      <div class="toolbar">
        <div class="field status-field">
          <label for="status-filter">Estado</label>
          <select id="status-filter" class="select" :value="status" @change="setStatus(($event.target as HTMLSelectElement).value)">
            <option value="">Todos los estados</option>
            <option v-for="value in ALL_TRIP_STATUSES" :key="value" :value="value">{{ tripStatusLabels[value] }}</option>
          </select>
        </div>
        <span class="spacer" />
        <button class="btn btn-secondary btn-sm" type="button" :disabled="loading" @click="load">
          <AppIcon name="refresh" :size="16" />
          Actualizar
        </button>
      </div>

      <AsyncState
        :loading="loading"
        :error="error"
        :empty="items.length === 0"
        :empty-text="status ? `No hay viajes en estado «${tripStatusLabels[status as TripStatus]}»` : 'Aún no hay viajes registrados'"
        @retry="load"
      >
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Solicitado</th>
                <th>Pasajero</th>
                <th>Conductor</th>
                <th class="hide-sm">Recorrido</th>
                <th class="num hide-sm">Distancia</th>
                <th class="num">Tarifa</th>
                <th>Estado</th>
                <th class="actions" />
              </tr>
            </thead>
            <tbody>
              <tr v-for="trip in items" :key="trip.id">
                <td class="nowrap">
                  <div class="cell-main">{{ formatDateTime(trip.requestedAt) }}</div>
                  <div class="cell-sub mono">{{ trip.id.slice(0, 8) }}</div>
                </td>
                <td class="nowrap">{{ fullName(trip.passenger) }}</td>
                <td class="nowrap">
                  <template v-if="trip.driver">
                    <div>{{ fullName(trip.driver) }}</div>
                    <div class="cell-sub">{{ trip.driver.vehicle?.plate }}</div>
                  </template>
                  <span v-else class="muted">Sin asignar</span>
                </td>
                <td class="hide-sm route-cell">
                  <div class="ellipsis">{{ shortAddress(trip.origin) }}</div>
                  <div class="ellipsis cell-sub">→ {{ shortAddress(trip.destination) }}</div>
                </td>
                <td class="num hide-sm">{{ formatDistance(trip.distanceMeters) }}</td>
                <td class="num">
                  <div class="cell-main">{{ formatMoney(trip.finalFare ?? trip.estimatedFare, trip.currency) }}</div>
                  <div v-if="trip.finalFare === null" class="cell-sub">estimada</div>
                </td>
                <td><StatusBadge type="trip" :value="trip.status" /></td>
                <td class="actions">
                  <RouterLink :to="`/trips/${trip.id}`" class="btn btn-ghost btn-sm">
                    Detalle
                    <AppIcon name="chevron-right" :size="16" />
                  </RouterLink>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </AsyncState>

      <PaginationBar v-if="!error && total > 0" :page="page" :size="size" :total="total" @change="goTo" />
    </section>
  </div>
</template>

<style scoped>
.status-field {
  min-width: 240px;
}

.route-cell {
  max-width: 280px;
}

.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn {
  text-decoration: none;
}
</style>
