<template>
    <div class="card">
        <h2>Hello {{ name }}!</h2>
        <button @click="$emit('tap')">Tap me</button>
    </div>
</template>

<script setup lang="ts">
interface Props { name: string }
defineProps<Props>()
defineEmits<{ (e: 'tap'): void }>()
</script>