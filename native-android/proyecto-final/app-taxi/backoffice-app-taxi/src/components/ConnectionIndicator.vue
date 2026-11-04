<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useRealtimeStore } from '@/stores/realtime'

const { state } = storeToRefs(useRealtimeStore())

const view = computed(() => {
  switch (state.value) {
    case 'connected':
      return { label: 'Tiempo real conectado', tone: 'ok' }
    case 'connecting':
      return { label: 'Conectando…', tone: 'wait' }
    case 'reconnecting':
      return { label: 'Reconectando…', tone: 'wait' }
    case 'error':
      return { label: 'Sin tiempo real', tone: 'off' }
    default:
      return { label: 'Desconectado', tone: 'off' }
  }
})
</script>

<template>
  <div class="connection" :class="view.tone" role="status">
    <span class="pulse" />
    {{ view.label }}
  </div>
</template>

<style scoped>
.connection {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 600;
}

.pulse {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.ok .pulse {
  background: #69f0ae;
  box-shadow: 0 0 0 0 rgba(105, 240, 174, 0.6);
  animation: pulse 2s infinite;
}

.wait .pulse {
  background: #ffd54f;
}

.off .pulse {
  background: #bdbdbd;
}

@keyframes pulse {
  70% {
    box-shadow: 0 0 0 8px rgba(105, 240, 174, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(105, 240, 174, 0);
  }
}
</style>
