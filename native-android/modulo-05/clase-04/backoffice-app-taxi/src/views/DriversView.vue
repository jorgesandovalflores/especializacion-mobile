<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import AsyncState from '@/components/AsyncState.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import BaseModal from '@/components/BaseModal.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import AppIcon from '@/components/AppIcon.vue'
import { driversApi } from '@/api/admin'
import { apiErrorMessage, apiFieldErrors } from '@/api/http'
import { usePagedQuery } from '@/composables/usePagedQuery'
import { useRealtime } from '@/composables/useRealtime'
import { useToastStore } from '@/stores/toasts'
import { formatPhone, fullName } from '@/utils/format'
import type { AdminDriverDto, DriverDto, UserStatus } from '@/types/models'

const toasts = useToastStore()

const { items, total, page, size, search, loading, error, load, goTo } = usePagedQuery(driversApi.list, {
  errorMessage: 'No se pudo cargar la lista de conductores'
})

const modalOpen = ref(false)
const editing = ref<AdminDriverDto | null>(null)
const saving = ref(false)
const fieldErrors = ref<Record<string, string>>({})
const form = reactive({
  givenName: '',
  familyName: '',
  phone: '',
  email: '',
  password: '',
  status: 'ACTIVE' as UserStatus,
  plate: '',
  brand: '',
  model: '',
  color: ''
})

const toDelete = ref<AdminDriverDto | null>(null)
const deleting = ref(false)

useRealtime('driver:availability', (event) => {
  if (!items.value.some((item) => item.id === event.driverId)) return
  items.value = items.value.map((item) => (item.id === event.driverId ? { ...item, online: event.online } : item))
})

function openCreate() {
  editing.value = null
  Object.assign(form, {
    givenName: '',
    familyName: '',
    phone: '',
    email: '',
    password: '',
    status: 'ACTIVE',
    plate: '',
    brand: '',
    model: '',
    color: ''
  })
  fieldErrors.value = {}
  modalOpen.value = true
}

function openEdit(item: AdminDriverDto) {
  editing.value = item
  Object.assign(form, {
    givenName: item.givenName,
    familyName: item.familyName,
    phone: item.phone,
    email: item.email ?? '',
    password: '',
    status: item.status,
    plate: item.vehicle?.plate ?? '',
    brand: item.vehicle?.brand ?? '',
    model: item.vehicle?.model ?? '',
    color: item.vehicle?.color ?? ''
  })
  fieldErrors.value = {}
  modalOpen.value = true
}

function validate(): boolean {
  const errors: Record<string, string> = {}
  if (!form.givenName.trim()) errors.givenName = 'Ingresa los nombres'
  if (!form.familyName.trim()) errors.familyName = 'Ingresa los apellidos'
  if (!/^\d{9,15}$/.test(form.phone.trim())) errors.phone = 'Solo dígitos con código de país, ej. 51900000001'
  if (form.email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) errors.email = 'Correo no válido'
  if (!editing.value && form.password.length < 8) errors.password = 'Mínimo 8 caracteres'
  if (editing.value && form.password && form.password.length < 8) errors.password = 'Mínimo 8 caracteres'
  if (!form.plate.trim()) errors['vehicle.plate'] = 'Ingresa la placa'
  if (!form.brand.trim()) errors['vehicle.brand'] = 'Ingresa la marca'
  if (!form.model.trim()) errors['vehicle.model'] = 'Ingresa el modelo'
  if (!form.color.trim()) errors['vehicle.color'] = 'Ingresa el color'
  fieldErrors.value = errors
  return Object.keys(errors).length === 0
}

function mergeSaved(saved: DriverDto) {
  items.value = items.value.map((item) => (item.id === saved.id ? { ...item, ...saved } : item))
}

async function save() {
  if (!validate()) return
  saving.value = true
  const vehicle = {
    plate: form.plate.trim().toUpperCase(),
    brand: form.brand.trim(),
    model: form.model.trim(),
    color: form.color.trim()
  }
  const base = {
    phone: form.phone.trim(),
    givenName: form.givenName.trim(),
    familyName: form.familyName.trim(),
    vehicle
  }
  const email = form.email.trim().toLowerCase()
  try {
    if (editing.value) {
      const saved = await driversApi.update(editing.value.id, {
        ...base,
        email: email || null,
        status: form.status,
        ...(form.password ? { password: form.password } : {})
      })
      mergeSaved(saved)
      toasts.success('Conductor actualizado')
    } else {
      await driversApi.create({ ...base, ...(email ? { email } : {}), password: form.password })
      toasts.success('Conductor creado')
      page.value = 1
      await load()
    }
    modalOpen.value = false
  } catch (err) {
    fieldErrors.value = apiFieldErrors(err)
    toasts.error(apiErrorMessage(err, 'No se pudo guardar el conductor'))
  } finally {
    saving.value = false
  }
}

async function confirmDelete() {
  if (!toDelete.value) return
  deleting.value = true
  try {
    await driversApi.remove(toDelete.value.id)
    toasts.success('Conductor eliminado')
    toDelete.value = null
    await load()
  } catch (err) {
    toasts.error(apiErrorMessage(err, 'No se pudo eliminar el conductor'))
  } finally {
    deleting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <PageHeader title="Conductores" subtitle="Cuentas de conductores, sus vehículos y si están en labores.">
      <template #actions>
        <button class="btn btn-primary" type="button" @click="openCreate">
          <AppIcon name="plus" />
          Nuevo conductor
        </button>
      </template>
    </PageHeader>

    <section class="card">
      <div class="toolbar">
        <div class="search-box">
          <AppIcon name="search" :size="18" />
          <input
            v-model="search"
            class="input"
            type="search"
            placeholder="Buscar por nombre, teléfono o placa"
            aria-label="Buscar conductores"
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
        :empty-text="search ? 'Ningún conductor coincide con la búsqueda' : 'Aún no hay conductores registrados'"
        @retry="load"
      >
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Conductor</th>
                <th>Teléfono</th>
                <th>Vehículo</th>
                <th class="num hide-sm">Calificación</th>
                <th>Estado</th>
                <th>En labores</th>
                <th class="actions">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in items" :key="item.id">
                <td>
                  <div class="cell-main">{{ fullName(item) }}</div>
                  <div class="cell-sub">{{ item.email || 'Sin correo' }}</div>
                </td>
                <td>{{ formatPhone(item.phone) }}</td>
                <td>
                  <span class="plate">{{ item.vehicle?.plate || '—' }}</span>
                  <div class="cell-sub">{{ [item.vehicle?.brand, item.vehicle?.model, item.vehicle?.color].filter(Boolean).join(' · ') }}</div>
                </td>
                <td class="num hide-sm">
                  <template v-if="item.rating !== null && item.ratingCount > 0">
                    ★ {{ item.rating.toFixed(1) }} <span class="cell-sub">({{ item.ratingCount }})</span>
                  </template>
                  <span v-else class="muted">—</span>
                </td>
                <td><StatusBadge type="user" :value="item.status" /></td>
                <td><StatusBadge type="online" :value="item.online" /></td>
                <td class="actions">
                  <button class="btn btn-ghost btn-icon" type="button" :aria-label="`Editar ${fullName(item)}`" @click="openEdit(item)">
                    <AppIcon name="edit" :size="18" />
                  </button>
                  <button
                    class="btn btn-ghost btn-icon danger"
                    type="button"
                    :aria-label="`Eliminar ${fullName(item)}`"
                    @click="toDelete = item"
                  >
                    <AppIcon name="trash" :size="18" />
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </AsyncState>

      <PaginationBar v-if="!error && total > 0" :page="page" :size="size" :total="total" @change="goTo" />
    </section>

    <BaseModal
      :open="modalOpen"
      :title="editing ? 'Editar conductor' : 'Nuevo conductor'"
      :width="640"
      :persistent="saving"
      @close="modalOpen = false"
    >
      <form id="driver-form" class="form-grid" novalidate @submit.prevent="save">
        <p class="form-section-title">Datos personales</p>
        <div class="field">
          <label for="driver-given">Nombres</label>
          <input id="driver-given" v-model="form.givenName" class="input" :class="{ invalid: fieldErrors.givenName }" />
          <span v-if="fieldErrors.givenName" class="error-text">{{ fieldErrors.givenName }}</span>
        </div>
        <div class="field">
          <label for="driver-family">Apellidos</label>
          <input id="driver-family" v-model="form.familyName" class="input" :class="{ invalid: fieldErrors.familyName }" />
          <span v-if="fieldErrors.familyName" class="error-text">{{ fieldErrors.familyName }}</span>
        </div>
        <div class="field">
          <label for="driver-phone">Teléfono</label>
          <input
            id="driver-phone"
            v-model="form.phone"
            class="input"
            :class="{ invalid: fieldErrors.phone }"
            inputmode="numeric"
            placeholder="51900000001"
          />
          <span v-if="fieldErrors.phone" class="error-text">{{ fieldErrors.phone }}</span>
          <span v-else class="hint">Con código de país; es el usuario de ingreso.</span>
        </div>
        <div class="field">
          <label for="driver-email">Correo (opcional)</label>
          <input id="driver-email" v-model="form.email" class="input" :class="{ invalid: fieldErrors.email }" type="email" />
          <span v-if="fieldErrors.email" class="error-text">{{ fieldErrors.email }}</span>
        </div>
        <div class="field" :class="{ 'span-2': !editing }">
          <label for="driver-password">{{ editing ? 'Nueva contraseña' : 'Contraseña' }}</label>
          <input
            id="driver-password"
            v-model="form.password"
            class="input"
            :class="{ invalid: fieldErrors.password }"
            type="password"
            autocomplete="new-password"
            :placeholder="editing ? 'Déjala vacía para no cambiarla' : ''"
          />
          <span v-if="fieldErrors.password" class="error-text">{{ fieldErrors.password }}</span>
          <span v-else class="hint">Mínimo 8 caracteres.</span>
        </div>
        <div v-if="editing" class="field">
          <label for="driver-status">Estado</label>
          <select id="driver-status" v-model="form.status" class="select">
            <option value="ACTIVE">Activo</option>
            <option value="INACTIVE">Inactivo</option>
          </select>
        </div>

        <p class="form-section-title">Vehículo</p>
        <div class="field">
          <label for="vehicle-plate">Placa</label>
          <input
            id="vehicle-plate"
            v-model="form.plate"
            class="input plate-input"
            :class="{ invalid: fieldErrors['vehicle.plate'] }"
            placeholder="ABC-123"
          />
          <span v-if="fieldErrors['vehicle.plate']" class="error-text">{{ fieldErrors['vehicle.plate'] }}</span>
        </div>
        <div class="field">
          <label for="vehicle-color">Color</label>
          <input id="vehicle-color" v-model="form.color" class="input" :class="{ invalid: fieldErrors['vehicle.color'] }" placeholder="Blanco" />
          <span v-if="fieldErrors['vehicle.color']" class="error-text">{{ fieldErrors['vehicle.color'] }}</span>
        </div>
        <div class="field">
          <label for="vehicle-brand">Marca</label>
          <input id="vehicle-brand" v-model="form.brand" class="input" :class="{ invalid: fieldErrors['vehicle.brand'] }" placeholder="Toyota" />
          <span v-if="fieldErrors['vehicle.brand']" class="error-text">{{ fieldErrors['vehicle.brand'] }}</span>
        </div>
        <div class="field">
          <label for="vehicle-model">Modelo</label>
          <input id="vehicle-model" v-model="form.model" class="input" :class="{ invalid: fieldErrors['vehicle.model'] }" placeholder="Yaris" />
          <span v-if="fieldErrors['vehicle.model']" class="error-text">{{ fieldErrors['vehicle.model'] }}</span>
        </div>
      </form>
      <template #footer>
        <button class="btn btn-secondary" type="button" :disabled="saving" @click="modalOpen = false">Cancelar</button>
        <button class="btn btn-primary" type="submit" form="driver-form" :disabled="saving">
          <span v-if="saving" class="spinner" />
          {{ editing ? 'Guardar cambios' : 'Crear conductor' }}
        </button>
      </template>
    </BaseModal>

    <ConfirmDialog
      :open="!!toDelete"
      title="Eliminar conductor"
      :message="`¿Seguro que deseas eliminar a ${toDelete ? fullName(toDelete) : ''}? Ya no podrá ingresar a la app del conductor.`"
      :loading="deleting"
      @cancel="toDelete = null"
      @confirm="confirmDelete"
    />
  </div>
</template>

<style scoped>
.plate {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 6px;
  border: 1px solid var(--color-outline);
  background: var(--color-surface-variant);
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.04em;
}

.plate-input {
  text-transform: uppercase;
}
</style>
