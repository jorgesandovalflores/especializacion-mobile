<script setup lang="ts">
import { storeToRefs } from 'pinia'
import AppIcon from './AppIcon.vue'
import { useToastStore } from '@/stores/toasts'

const store = useToastStore()
const { items } = storeToRefs(store)
</script>

<template>
  <div class="toast-host" aria-live="polite">
    <TransitionGroup name="toast">
      <div v-for="toast in items" :key="toast.id" class="toast" :class="toast.kind" role="status">
        <AppIcon :name="toast.kind === 'success' ? 'check' : toast.kind === 'error' ? 'alert' : 'info'" />
        <span class="toast-message">{{ toast.message }}</span>
        <button class="toast-close" type="button" aria-label="Cerrar aviso" @click="store.dismiss(toast.id)">
          <AppIcon name="close" :size="16" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-host {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 2000;
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: min(380px, calc(100vw - 32px));
}

.toast {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 14px 14px 16px;
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-on-surface);
  box-shadow: var(--shadow-lg);
  border-left: 5px solid var(--color-info);
}

.toast.success {
  border-left-color: var(--color-success);
}

.toast.success svg:first-child {
  color: var(--color-success);
}

.toast.error {
  border-left-color: var(--color-error);
}

.toast.error svg:first-child {
  color: var(--color-error);
}

.toast-message {
  flex: 1;
  font-weight: 500;
}

.toast-close {
  border: 0;
  background: transparent;
  color: var(--color-on-surface-variant);
  cursor: pointer;
  padding: 4px;
  border-radius: 8px;
  display: grid;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.25s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(12px);
}
</style>
