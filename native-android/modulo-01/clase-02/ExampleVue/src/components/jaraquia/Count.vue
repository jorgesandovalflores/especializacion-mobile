<template>
    <div class="panel">
        <p>Count: {{ count }}</p>
        <div class="row">
            <button @click="inc">Increment</button>
            <button @click="reset">Reset</button>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
const count = ref(0)
const inc = () => count.value++
const reset = () => (count.value = 0)
</script>