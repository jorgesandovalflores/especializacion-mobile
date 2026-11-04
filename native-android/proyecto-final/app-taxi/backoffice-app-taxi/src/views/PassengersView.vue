<script setup lang="ts">
import { onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import AsyncState from '@/components/AsyncState.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import AppIcon from '@/components/AppIcon.vue'
import { passengersApi } from '@/api/admin'
import { usePagedQuery } from '@/composables/usePagedQuery'
import { formatDateTime, formatPhone, fullName, relativeTime } from '@/utils/format'

const { items, total, page, size, search, loading, error, load, goTo } = usePagedQuery(passengersApi.list, {
  errorMessage: 'No se pudo cargar la lista de pasajeros'
})

onMounted(load)
</script>

<template>
  <div class="page">
    <PageHeader title="Pasajeros" subtitle="Usuarios registrados en la app del pasajero (solo lectura)." />

    <section class="card">
      <div class="toolbar">
        <div class="search-box">
          <AppIcon name="search" :size="18" />
          <input
            v-model="search"
            class="input"
            type="search"
            placeholder="Buscar por nombre, teléfono o correo"
            aria-label="Buscar pasajeros"
          />
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
        :empty-text="search ? 'Ningún pasajero coincide con la búsqueda' : 'Aún no hay pasajeros registrados'"
        @retry="load"
      >
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Pasajero</th>
                <th>Teléfono</th>
                <th>Estado</th>
                <th class="hide-sm">Último acceso</th>
                <th class="hide-sm">Registrado</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in items" :key="item.id">
                <td>
                  <div class="person">
                    <img v-if="item.photoUrl" :src="item.photoUrl" alt="" class="avatar-sm" />
                    <span v-else class="avatar-sm placeholder">{{ (item.givenName || '?')[0] }}</span>
                    <div>
                      <div class="cell-main">{{ fullName(item) }}</div>
                      <div class="cell-sub">{{ item.email || 'Sin correo' }}</div>
                    </div>
                  </div>
                </td>
                <td>{{ formatPhone(item.phone) }}</td>
                <td><StatusBadge type="user" :value="item.status" /></td>
                <td class="hide-sm" :title="formatDateTime(item.lastLoginAt)">{{ relativeTime(item.lastLoginAt) }}</td>
                <td class="hide-sm">{{ formatDateTime(item.createdAt) }}</td>
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
.person {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-sm {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.avatar-sm.placeholder {
  display: grid;
  place-items: center;
  background: var(--color-primary-soft);
  color: var(--color-primary);
  font-weight: 800;
  text-transform: uppercase;
}
</style>
