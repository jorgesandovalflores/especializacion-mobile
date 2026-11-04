<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import logoUrl from '@/assets/feature_splash_logo.png'
import AppIcon from '@/components/AppIcon.vue'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toasts'
import { apiErrorMessage } from '@/api/http'

const auth = useAuthStore()
const toasts = useToastStore()
const router = useRouter()
const route = useRoute()

const form = reactive({ email: '', password: '' })
const loading = ref(false)
const error = ref<string | null>(null)
const showPassword = ref(false)

async function submit() {
  error.value = null
  if (!form.email.trim() || !form.password) {
    error.value = 'Ingresa tu correo y contraseña'
    return
  }
  loading.value = true
  try {
    await auth.login(form.email.trim().toLowerCase(), form.password)
    toasts.success(`Bienvenido, ${auth.admin?.fullName ?? 'administrador'}`)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.replace(redirect.startsWith('/') ? redirect : '/')
  } catch (err) {
    error.value = apiErrorMessage(err, 'No se pudo iniciar sesión')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login">
    <section class="hero">
      <img :src="logoUrl" alt="App Taxi" class="hero-logo" />
      <h1>Backoffice</h1>
      <p>Monitorea conductores y viajes en tiempo real, administra usuarios y configura la tarifa.</p>
      <ul class="hero-points">
        <li><span class="dot free" /> Conductores libres</li>
        <li><span class="dot busy" /> Conductores en viaje</li>
        <li><span class="dot offline" /> Fuera de labores</li>
      </ul>
    </section>

    <section class="panel">
      <form class="form card" novalidate @submit.prevent="submit">
        <div>
          <h2>Iniciar sesión</h2>
          <p class="muted">Ingresa con tu cuenta de administrador.</p>
        </div>

        <div class="field">
          <label for="email">Correo electrónico</label>
          <input
            id="email"
            v-model="form.email"
            class="input"
            type="email"
            autocomplete="username"
            placeholder="admin@apptaxi.pe"
            :disabled="loading"
          />
        </div>

        <div class="field">
          <label for="password">Contraseña</label>
          <div class="password">
            <input
              id="password"
              v-model="form.password"
              class="input"
              :type="showPassword ? 'text' : 'password'"
              autocomplete="current-password"
              placeholder="••••••••"
              :disabled="loading"
            />
            <button
              class="password-toggle"
              type="button"
              :aria-label="showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'"
              @click="showPassword = !showPassword"
            >
              {{ showPassword ? 'Ocultar' : 'Mostrar' }}
            </button>
          </div>
        </div>

        <p v-if="error" class="form-error" role="alert">
          <AppIcon name="alert" :size="18" />
          {{ error }}
        </p>

        <button class="btn btn-primary submit" type="submit" :disabled="loading">
          <span v-if="loading" class="spinner" />
          {{ loading ? 'Ingresando…' : 'Ingresar' }}
        </button>
      </form>
    </section>
  </div>
</template>

<style scoped>
.login {
  min-height: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
}

.hero {
  background: var(--sidebar-bg);
  color: #ffffff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px clamp(32px, 6vw, 96px);
  gap: 12px;
}

.hero-logo {
  width: min(340px, 80%);
  aspect-ratio: 1100 / 640;
  object-fit: cover;
  margin-left: -12px;
}

.hero h1 {
  font-size: 18px;
  letter-spacing: 0.3em;
  text-transform: uppercase;
  color: var(--color-secondary);
}

.hero p {
  margin: 0;
  max-width: 420px;
  font-size: 17px;
  color: rgba(255, 255, 255, 0.86);
}

.hero-points {
  list-style: none;
  margin: 20px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-weight: 600;
}

.hero-points li {
  display: flex;
  align-items: center;
  gap: 10px;
}

.panel {
  display: grid;
  place-items: center;
  padding: 32px 20px;
}

.form {
  width: min(420px, 100%);
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 32px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
}

.form h2 {
  font-size: 26px;
  font-weight: 800;
}

.form p.muted {
  margin: 6px 0 0;
}

.password {
  position: relative;
}

.password .input {
  padding-right: 92px;
}

.password-toggle {
  position: absolute;
  right: 6px;
  top: 50%;
  transform: translateY(-50%);
  border: 0;
  background: transparent;
  color: var(--color-primary);
  font-weight: 700;
  font-size: 13px;
  padding: 8px 10px;
  border-radius: 10px;
  cursor: pointer;
}

.form-error {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  padding: 12px 14px;
  border-radius: var(--radius-sm);
  background: var(--color-error-soft);
  color: var(--color-error);
  font-weight: 600;
  font-size: 14px;
}

.submit {
  width: 100%;
  min-height: 52px;
  font-size: 16px;
}

@media (max-width: 860px) {
  .login {
    grid-template-columns: 1fr;
  }

  .hero {
    padding: 32px 24px;
    align-items: center;
    text-align: center;
  }

  .hero-logo {
    width: 200px;
    margin-left: 0;
  }

  .hero-points {
    display: none;
  }
}
</style>
