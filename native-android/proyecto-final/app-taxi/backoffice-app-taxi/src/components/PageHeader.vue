<script setup lang="ts">
defineProps<{ title: string; subtitle?: string }>()
</script>

<template>
  <header class="page-header">
    <div class="page-header-text">
      <slot name="before" />
      <h1>{{ title }}</h1>
      <p v-if="subtitle">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.actions" class="page-header-actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.page-full .page-header {
  margin-bottom: 0;
}

.page-header h1 {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.01em;
}

.page-header p {
  margin: 4px 0 0;
  color: var(--color-on-surface-variant);
}

.page-header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}
</style>
