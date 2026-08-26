<template>
  <form class="login-card" @submit.prevent="onSubmit">
    <h2>Iniciar sesión</h2>

    <label class="field">
      <span>Email</span>
      <input
        v-model="email"
        type="email"
        placeholder="tu@correo.com"
        autocomplete="username"
      />
      <small v-if="email.length > 0 && !emailValid" class="error">
        Ingresa un email válido.
      </small>
    </label>

    <label class="field">
      <span>Password</span>
      <input
        v-model="password"
        type="password"
        placeholder="Mínimo 6 caracteres"
        autocomplete="current-password"
      />
      <small v-if="password.length > 0 && !passwordValid" class="error">
        La contraseña debe tener al menos 6 caracteres.
      </small>
    </label>

    <button class="submit" type="submit" :disabled="!formValid || isLoading">
      {{ isLoading ? 'Ingresando...' : 'Iniciar sesión' }}
    </button>

    <p v-if="resultMessage" class="result">{{ resultMessage }}</p>
  </form>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const PASSWORD_MIN_LENGTH = 6

const email = ref('')
const password = ref('')
const isLoading = ref(false)
const resultMessage = ref<string | null>(null)

const emailValid = computed(() => EMAIL_PATTERN.test(email.value))
const passwordValid = computed(() => password.value.length >= PASSWORD_MIN_LENGTH)
const formValid = computed(() => emailValid.value && passwordValid.value)

async function onSubmit() {
  if (!formValid.value || isLoading.value) return

  isLoading.value = true
  resultMessage.value = null

  // Simulación de una llamada de red (sin backend real)
  await new Promise((resolve) => setTimeout(resolve, 900))

  isLoading.value = false
  resultMessage.value = `Bienvenido, ${email.value}`
}
</script>

<style scoped>
.login-card {
  max-width: 320px;
  margin: 0 auto;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  gap: 16px;
  text-align: left;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
}
.field input {
  padding: 10px 12px;
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 16px;
}
.error {
  color: #b3261e;
}
.submit {
  padding: 12px;
  border: none;
  border-radius: 8px;
  background: #0066cc;
  color: white;
  font-weight: 600;
  cursor: pointer;
}
.submit:disabled {
  background: #9db8d8;
  cursor: not-allowed;
}
.result {
  margin: 0;
  color: #1e7a34;
  font-weight: 600;
}
</style>
