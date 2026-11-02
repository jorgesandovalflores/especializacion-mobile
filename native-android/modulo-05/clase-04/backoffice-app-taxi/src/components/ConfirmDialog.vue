<script setup lang="ts">
import BaseModal from './BaseModal.vue'

withDefaults(
  defineProps<{
    open: boolean
    title: string
    message: string
    confirmLabel?: string
    loading?: boolean
  }>(),
  { confirmLabel: 'Eliminar', loading: false }
)

const emit = defineEmits<{ confirm: []; cancel: [] }>()
</script>

<template>
  <BaseModal :open="open" :title="title" :width="440" :persistent="loading" @close="emit('cancel')">
    <p class="confirm-message">{{ message }}</p>
    <template #footer>
      <button class="btn btn-secondary" type="button" :disabled="loading" @click="emit('cancel')">Cancelar</button>
      <button class="btn btn-danger" type="button" :disabled="loading" @click="emit('confirm')">
        <span v-if="loading" class="spinner" />
        {{ confirmLabel }}
      </button>
    </template>
  </BaseModal>
</template>

<style scoped>
.confirm-message {
  margin: 0;
  color: var(--color-on-surface-variant);
}
</style>
