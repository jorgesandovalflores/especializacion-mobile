<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { storeToRefs } from 'pinia'
import PageHeader from '@/components/PageHeader.vue'
import AsyncState from '@/components/AsyncState.vue'
import BaseModal from '@/components/BaseModal.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import AppIcon from '@/components/AppIcon.vue'
import { adminsApi } from '@/api/admin'
import { apiErrorMessage, apiFieldErrors } from '@/api/http'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toasts'
import { formatDateTime, relativeTime } from '@/utils/format'
import type { AdminDto, UserStatus } from '@/types/models'

const auth = useAuthStore()
const toasts = useToastStore()
const { admin: currentAdmin } = storeToRefs(auth)

const admins = ref<AdminDto[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const search = ref('')

const modalOpen = ref(false)
const editing = ref<AdminDto | null>(null)
const saving = ref(false)
const form = reactive({ fullName: '', email: '', password: '', status: 'ACTIVE' as UserStatus })
const fieldErrors = ref<Record<string, string>>({})

const toDelete = ref<AdminDto | null>(null)
const deleting = ref(false)

const filtered = computed(() => {
  const term = search.value.trim().toLowerCase()
  if (!term) return admins.value
  return admins.value.filter(
    (item) => item.fullName.toLowerCase().includes(term) || item.email.toLowerCase().includes(term)
  )
})

const isSelf = (item: AdminDto) => item.id === currentAdmin.value?.id

async function load() {
  loading.value = true
  error.value = null
  try {
    admins.value = await adminsApi.list()
  } catch (err) {
    error.value = apiErrorMessage(err, 'No se pudo cargar la lista de administradores')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = null
  Object.assign(form, { fullName: '', email: '', password: '', status: 'ACTIVE' })
  fieldErrors.value = {}
  modalOpen.value = true
}

function openEdit(item: AdminDto) {
  editing.value = item
  Object.assign(form, { fullName: item.fullName, email: item.email, password: '', status: item.status })
  fieldErrors.value = {}
  modalOpen.value = true
}

function validate(): boolean {
  const errors: Record<string, string> = {}
  if (form.fullName.trim().length < 3) errors.fullName = 'Ingresa el nombre completo'
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) errors.email = 'Ingresa un correo válido'
  if (!editing.value && form.password.length < 8) errors.password = 'Mínimo 8 caracteres'
  if (editing.value && form.password && form.password.length < 8) errors.password = 'Mínimo 8 caracteres'
  fieldErrors.value = errors
  return Object.keys(errors).length === 0
}

async function save() {
  if (!validate()) return
  saving.value = true
  try {
    const fullName = form.fullName.trim()
    const email = form.email.trim().toLowerCase()
    if (editing.value) {
      const updated = await adminsApi.update(editing.value.id, {
        fullName,
        email,
        status: form.status,
        ...(form.password ? { password: form.password } : {})
      })
      admins.value = admins.value.map((item) => (item.id === updated.id ? updated : item))
      if (isSelf(updated)) auth.updateAdmin(updated)
      toasts.success('Administrador actualizado')
    } else {
      const created = await adminsApi.create({ fullName, email, password: form.password })
      admins.value = [created, ...admins.value]
      toasts.success('Administrador creado')
    }
    modalOpen.value = false
  } catch (err) {
    fieldErrors.value = apiFieldErrors(err)
    toasts.error(apiErrorMessage(err, 'No se pudo guardar el administrador'))
  } finally {
    saving.value = false
  }
}

async function confirmDelete() {
  if (!toDelete.value) return
  deleting.value = true
  try {
    await adminsApi.remove(toDelete.value.id)
    admins.value = admins.value.filter((item) => item.id !== toDelete.value?.id)
    toasts.success('Administrador eliminado')
    toDelete.value = null
  } catch (err) {
    toasts.error(apiErrorMessage(err, 'No se pudo eliminar el administrador'))
  } finally {
    deleting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <PageHeader title="Administradores" subtitle="Cuentas con acceso al backoffice.">
      <template #actions>
        <button class="btn btn-primary" type="button" @click="openCreate">
          <AppIcon name="plus" />
          Nuevo administrador
        </button>
      </template>
    </PageHeader>

    <section class="card">
      <div class="toolbar">
        <div class="search-box">
          <AppIcon name="search" :size="18" />
          <input v-model="search" class="input" type="search" placeholder="Buscar por nombre o correo" aria-label="Buscar" />
        </div>
        <span class="spacer" />
        <span class="muted">{{ filtered.length }} administradores</span>
      </div>

      <AsyncState
        :loading="loading"
        :error="error"
        :empty="filtered.length === 0"
        :empty-text="search ? 'Ningún administrador coincide con la búsqueda' : 'Aún no hay administradores'"
        @retry="load"
      >
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Estado</th>
                <th class="hide-sm">Último acceso</th>
                <th class="hide-sm">Creado</th>
                <th class="actions">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in filtered" :key="item.id">
                <td>
                  <div class="cell-main">
                    {{ item.fullName }}
                    <span v-if="isSelf(item)" class="badge primary self">Tú</span>
                  </div>
                  <div class="cell-sub">{{ item.email }}</div>
                </td>
                <td><StatusBadge type="user" :value="item.status" /></td>
                <td class="hide-sm" :title="formatDateTime(item.lastLoginAt)">{{ relativeTime(item.lastLoginAt) }}</td>
                <td class="hide-sm">{{ formatDateTime(item.createdAt) }}</td>
                <td class="actions">
                  <button class="btn btn-ghost btn-icon" type="button" :aria-label="`Editar ${item.fullName}`" @click="openEdit(item)">
                    <AppIcon name="edit" :size="18" />
                  </button>
                  <button
                    class="btn btn-ghost btn-icon danger"
                    type="button"
                    :disabled="isSelf(item)"
                    :title="isSelf(item) ? 'No puedes eliminar tu propia cuenta' : 'Eliminar'"
                    :aria-label="`Eliminar ${item.fullName}`"
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
    </section>

    <BaseModal
      :open="modalOpen"
      :title="editing ? 'Editar administrador' : 'Nuevo administrador'"
      :persistent="saving"
      @close="modalOpen = false"
    >
      <form id="admin-form" class="form-grid" novalidate @submit.prevent="save">
        <div class="field span-2">
          <label for="admin-name">Nombre completo</label>
          <input id="admin-name" v-model="form.fullName" class="input" :class="{ invalid: fieldErrors.fullName }" autocomplete="off" />
          <span v-if="fieldErrors.fullName" class="error-text">{{ fieldErrors.fullName }}</span>
        </div>
        <div class="field span-2">
          <label for="admin-email">Correo electrónico</label>
          <input id="admin-email" v-model="form.email" class="input" :class="{ invalid: fieldErrors.email }" type="email" autocomplete="off" />
          <span v-if="fieldErrors.email" class="error-text">{{ fieldErrors.email }}</span>
        </div>
        <div class="field" :class="{ 'span-2': !editing }">
          <label for="admin-password">{{ editing ? 'Nueva contraseña' : 'Contraseña' }}</label>
          <input
            id="admin-password"
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
          <label for="admin-status">Estado</label>
          <select id="admin-status" v-model="form.status" class="select" :disabled="isSelf(editing)">
            <option value="ACTIVE">Activo</option>
            <option value="INACTIVE">Inactivo</option>
          </select>
          <span v-if="isSelf(editing)" class="hint">No puedes desactivar tu propia cuenta.</span>
        </div>
      </form>
      <template #footer>
        <button class="btn btn-secondary" type="button" :disabled="saving" @click="modalOpen = false">Cancelar</button>
        <button class="btn btn-primary" type="submit" form="admin-form" :disabled="saving">
          <span v-if="saving" class="spinner" />
          {{ editing ? 'Guardar cambios' : 'Crear administrador' }}
        </button>
      </template>
    </BaseModal>

    <ConfirmDialog
      :open="!!toDelete"
      title="Eliminar administrador"
      :message="`¿Seguro que deseas eliminar a ${toDelete?.fullName ?? ''}? Perderá el acceso al backoffice.`"
      :loading="deleting"
      @cancel="toDelete = null"
      @confirm="confirmDelete"
    />
  </div>
</template>

<style scoped>
.self {
  margin-left: 6px;
  padding: 2px 8px;
  font-size: 11px;
}

.self::before {
  display: none;
}
</style>
