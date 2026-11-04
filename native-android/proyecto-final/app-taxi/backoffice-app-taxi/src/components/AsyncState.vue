<script setup lang="ts">
import AppIcon from './AppIcon.vue'

withDefaults(
  defineProps<{
    loading: boolean
    error: string | null
    empty?: boolean
    emptyText?: string
    loadingText?: string
  }>(),
  { empty: false, emptyText: 'No hay registros para mostrar', loadingText: 'Cargando…' }
)

const emit = defineEmits<{ retry: [] }>()
</script>

<template>
  <div v-if="loading" class="state">
    <span class="spinner lg" />
    <p>{{ loadingText }}</p>
  </div>
  <div v-else-if="error" class="state error">
    <AppIcon name="alert" :size="32" />
    <p>{{ error }}</p>
    <button class="btn btn-secondary btn-sm" type="button" @click="emit('retry')">
      <AppIcon name="refresh" :size="16" />
      Reintentar
    </button>
  </div>
  <div v-else-if="empty" class="state">
    <AppIcon name="inbox" :size="32" />
    <p>{{ emptyText }}</p>
    <slot name="empty-action" />
  </div>
  <slot v-else />
</template>

<style scoped>
.state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 48px 24px;
  text-align: center;
  color: var(--color-on-surface-variant);
}

.state p {
  margin: 0;
  max-width: 420px;
}

.state.error svg {
  color: var(--color-error);
}
</style>
