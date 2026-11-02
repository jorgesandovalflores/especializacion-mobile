<script setup lang="ts">
import { computed } from 'vue'
import AppIcon from './AppIcon.vue'

const props = defineProps<{ page: number; size: number; total: number }>()
const emit = defineEmits<{ change: [page: number] }>()

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.size)))
const from = computed(() => (props.total === 0 ? 0 : (props.page - 1) * props.size + 1))
const to = computed(() => Math.min(props.total, props.page * props.size))
</script>

<template>
  <div class="pagination">
    <span class="muted">{{ from }}–{{ to }} de {{ total }}</span>
    <div class="row">
      <button
        class="btn btn-secondary btn-icon"
        type="button"
        aria-label="Página anterior"
        :disabled="page <= 1"
        @click="emit('change', page - 1)"
      >
        <AppIcon name="chevron-left" />
      </button>
      <span class="page-indicator">Página {{ page }} de {{ totalPages }}</span>
      <button
        class="btn btn-secondary btn-icon"
        type="button"
        aria-label="Página siguiente"
        :disabled="page >= totalPages"
        @click="emit('change', page + 1)"
      >
        <AppIcon name="chevron-right" />
      </button>
    </div>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  padding: 12px 20px;
  border-top: 1px solid var(--color-outline-soft);
  font-size: 14px;
}

.page-indicator {
  font-weight: 600;
  padding: 0 8px;
}
</style>
