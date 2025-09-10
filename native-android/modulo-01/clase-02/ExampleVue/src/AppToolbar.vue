<script setup lang="ts">
import ToolbarCustom from './components/custom/ToolbarCustom.vue'
import { computed, type CSSProperties } from 'vue'

const toolbarFixedStyle = computed<CSSProperties>(() => ({
  position: 'fixed',
  top: '0',
  left: '0',
  right: '0',
  width: '100%',
  zIndex: 1000,
  borderRadius: '0',
}))
</script>

<template>
  <ToolbarCustom
    :style="toolbarFixedStyle"
    :radius="0"
    :elevation="0"
    :barInsetX="0"
    :centerTitle="true"
    title="Title label"
  />

  <main class="content">
    <p>Contenido de la página debajo del toolbar...</p>
  </main>
</template>

<style>
.content { margin-top: 60px; }
</style>
