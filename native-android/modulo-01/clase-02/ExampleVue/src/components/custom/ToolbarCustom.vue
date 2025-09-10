<template>
  <!-- Card contenedor -->
  <div class="toolbar-card" :style="cardStyle">
    <!-- Contenido del título (con padding, sin afectar a la barra) -->
    <div :style="contentStyle" class="title">{{ title }}</div>

    <!-- Barra inferior multicolor, pegada al borde inferior -->
    <div :style="barWrapperStyle" class="bar-wrapper">
      <div
        v-for="(seg, i) in normalizedSegments"
        :key="i"
        :style="segStyle(seg)"
        class="bar-seg"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, type CSSProperties } from 'vue'

interface Segment { color: string; ratio?: number }

const props = withDefaults(defineProps<{
  title?: string
  height?: number
  radius?: number
  barHeight?: number
  elevation?: number
  paddingX?: number
  paddingY?: number         // padding vertical SOLO para el área del título
  barInsetX?: number        // margen horizontal de la barra (px)
  centerTitle?: boolean     // centrar título
  segments?: Segment[]
}>(), {
  title: 'Title label',
  height: 60,
  radius: 14,
  barHeight: 8,
  elevation: 16,
  paddingX: 24,
  paddingY: 16,
  barInsetX: 24,
  centerTitle: true,
  segments: () => ([
    { color: '#F5B700', ratio: 0.12 },
    { color: '#18AEF5', ratio: 0.10 },
    { color: '#4B007D', ratio: 0.78 },
  ]),
})

/* Normalización de ratios */
const normalizedSegments = computed(() => {
  const segs = props.segments ?? []
  const total = segs.reduce((acc, s) => acc + (s.ratio ?? 0), 0)
  if (!total) {
    const r = segs.length ? 1 / segs.length : 1
    return segs.map(s => ({ color: s.color, ratio: r }))
  }
  return segs.map(s => ({ color: s.color, ratio: (s.ratio ?? 0) / total }))
})

/* CARD: sin padding vertical para que la barra pueda pegarse abajo */
const cardStyle = computed<CSSProperties>(() => ({
  position: 'relative',
  height: `${props.height}px`,
  borderRadius: `${props.radius}px`,
  boxShadow: `0 ${props.elevation! / 4}px ${props.elevation}px rgba(0,0,0,.2)`,
  background: '#fff',
  overflow: 'hidden',
}))

/* CONTENIDO: padding solo para el title */
const contentStyle = computed<CSSProperties>(() => ({
  padding: `${props.paddingY}px ${props.paddingX}px 0 ${props.paddingX}px`,
  fontWeight: 800,
  fontSize: '28px',
  lineHeight: 1.2,
  color: '#222',
  textAlign: props.centerTitle ? 'center' : 'left',
}))

/* BARRA: posición absoluta al fondo, con inset horizontal configurable */
const barWrapperStyle = computed<CSSProperties>(() => ({
  position: 'absolute',
  left: `${props.barInsetX}px`,
  right: `${props.barInsetX}px`,
  bottom: 0,
  height: `${props.barHeight}px`,
  display: 'flex',
  overflow: 'hidden',
  borderRadius: '6px',
}))

function segStyle(seg: { color: string; ratio: number }): CSSProperties {
  return {
    background: seg.color,
    width: `${seg.ratio * 100}%`,
    height: '100%',
  }
}
</script>

<style scoped>
.toolbar-card { display: block; }
.bar-seg { display: block; }
</style>
