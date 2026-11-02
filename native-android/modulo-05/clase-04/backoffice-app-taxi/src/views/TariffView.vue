<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import AsyncState from '@/components/AsyncState.vue'
import AppIcon from '@/components/AppIcon.vue'
import { tariffApi } from '@/api/admin'
import { apiErrorMessage, apiFieldErrors } from '@/api/http'
import { useToastStore } from '@/stores/toasts'
import { formatDateTime, formatMoney, round2 } from '@/utils/format'
import type { TariffDto } from '@/types/models'

const toasts = useToastStore()

const tariff = ref<TariffDto | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const saving = ref(false)
const form = reactive({ baseFare: '', pricePerKm: '' })
const fieldErrors = ref<Record<string, string>>({})
const km = ref('8')

const presets = [2, 5, 10, 15, 25]

const baseFare = computed(() => Number.parseFloat(form.baseFare))
const pricePerKm = computed(() => Number.parseFloat(form.pricePerKm))
const kmValue = computed(() => Number.parseFloat(km.value))

const formValid = computed(
  () => Number.isFinite(baseFare.value) && baseFare.value >= 0 && Number.isFinite(pricePerKm.value) && pricePerKm.value >= 0
)

const dirty = computed(
  () => !!tariff.value && (baseFare.value !== tariff.value.baseFare || pricePerKm.value !== tariff.value.pricePerKm)
)

function quote(base: number, perKm: number, kilometers: number) {
  return round2(base + (perKm * kilometers * 1000) / 1000)
}

const preview = computed(() => {
  if (!formValid.value || !Number.isFinite(kmValue.value) || kmValue.value < 0) return null
  return quote(baseFare.value, pricePerKm.value, kmValue.value)
})

const currentPreview = computed(() => {
  if (!tariff.value || !Number.isFinite(kmValue.value) || kmValue.value < 0) return null
  return quote(tariff.value.baseFare, tariff.value.pricePerKm, kmValue.value)
})

const table = computed(() =>
  presets.map((value) => ({
    km: value,
    current: tariff.value ? quote(tariff.value.baseFare, tariff.value.pricePerKm, value) : null,
    next: formValid.value ? quote(baseFare.value, pricePerKm.value, value) : null
  }))
)

function fill(value: TariffDto) {
  tariff.value = value
  form.baseFare = value.baseFare.toFixed(2)
  form.pricePerKm = value.pricePerKm.toFixed(2)
}

async function load() {
  loading.value = true
  error.value = null
  try {
    fill(await tariffApi.get())
  } catch (err) {
    error.value = apiErrorMessage(err, 'No se pudo cargar la tarifa')
  } finally {
    loading.value = false
  }
}

async function save() {
  fieldErrors.value = {}
  if (!formValid.value) {
    fieldErrors.value = {
      ...(!(baseFare.value >= 0) ? { baseFare: 'Ingresa un monto válido (0 o mayor)' } : {}),
      ...(!(pricePerKm.value >= 0) ? { pricePerKm: 'Ingresa un monto válido (0 o mayor)' } : {})
    }
    return
  }
  saving.value = true
  try {
    fill(await tariffApi.update({ baseFare: round2(baseFare.value), pricePerKm: round2(pricePerKm.value) }))
    toasts.success('Tarifa actualizada')
  } catch (err) {
    fieldErrors.value = apiFieldErrors(err)
    toasts.error(apiErrorMessage(err, 'No se pudo guardar la tarifa'))
  } finally {
    saving.value = false
  }
}

function reset() {
  if (tariff.value) fill(tariff.value)
  fieldErrors.value = {}
}

onMounted(load)
</script>

<template>
  <div class="page">
    <PageHeader title="Tarifa" subtitle="Precio base y precio por kilómetro con los que se estima cada viaje." />

    <AsyncState :loading="loading" :error="error" @retry="load">
      <div class="tariff-grid">
        <section class="card card-body">
          <h2 class="card-title">Configuración</h2>
          <form class="stack" novalidate @submit.prevent="save">
            <div class="field">
              <label for="base-fare">Tarifa base</label>
              <div class="money-input">
                <span>S/</span>
                <input
                  id="base-fare"
                  v-model="form.baseFare"
                  class="input"
                  :class="{ invalid: fieldErrors.baseFare }"
                  type="number"
                  min="0"
                  step="0.10"
                  inputmode="decimal"
                />
              </div>
              <span v-if="fieldErrors.baseFare" class="error-text">{{ fieldErrors.baseFare }}</span>
              <span v-else class="hint">Monto fijo que se cobra al iniciar cualquier viaje.</span>
            </div>
            <div class="field">
              <label for="price-km">Precio por kilómetro</label>
              <div class="money-input">
                <span>S/</span>
                <input
                  id="price-km"
                  v-model="form.pricePerKm"
                  class="input"
                  :class="{ invalid: fieldErrors.pricePerKm }"
                  type="number"
                  min="0"
                  step="0.10"
                  inputmode="decimal"
                />
              </div>
              <span v-if="fieldErrors.pricePerKm" class="error-text">{{ fieldErrors.pricePerKm }}</span>
              <span v-else class="hint">Se multiplica por la distancia de la ruta calculada con OSRM.</span>
            </div>

            <div class="formula">
              <code>tarifa = redondear( base + precioKm × metros / 1000 , 2 )</code>
            </div>

            <p class="cell-sub" style="margin: 0">Última actualización: {{ formatDateTime(tariff?.updatedAt) }}</p>

            <div class="row">
              <button class="btn btn-secondary" type="button" :disabled="saving || !dirty" @click="reset">Descartar</button>
              <button class="btn btn-primary grow" type="submit" :disabled="saving || !dirty">
                <span v-if="saving" class="spinner" />
                Guardar tarifa
              </button>
            </div>
          </form>
        </section>

        <section class="card card-body">
          <h2 class="card-title row"><AppIcon name="calculator" :size="18" /> Calculadora de vista previa</h2>
          <div class="field">
            <label for="km">Distancia del viaje (km)</label>
            <input id="km" v-model="km" class="input" type="number" min="0" step="0.5" inputmode="decimal" />
            <input v-model="km" class="range" type="range" min="0" max="40" step="0.5" aria-label="Distancia en kilómetros" />
          </div>

          <div class="preview">
            <div class="preview-main">
              <span class="stat-label">Precio estimado {{ dirty ? 'con los nuevos valores' : '' }}</span>
              <strong>{{ formatMoney(preview, tariff?.currency) }}</strong>
            </div>
            <div v-if="dirty" class="preview-compare">
              <span class="stat-label">Con la tarifa vigente</span>
              <strong>{{ formatMoney(currentPreview, tariff?.currency) }}</strong>
            </div>
          </div>

          <table class="table compact">
            <thead>
              <tr>
                <th>Distancia</th>
                <th class="num">Vigente</th>
                <th v-if="dirty" class="num">Nueva</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in table" :key="row.km">
                <td>{{ row.km }} km</td>
                <td class="num">{{ formatMoney(row.current, tariff?.currency) }}</td>
                <td v-if="dirty" class="num accent">{{ formatMoney(row.next, tariff?.currency) }}</td>
              </tr>
            </tbody>
          </table>
        </section>
      </div>
    </AsyncState>
  </div>
</template>

<style scoped>
.tariff-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  align-items: start;
}

.money-input {
  position: relative;
}

.money-input span {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-weight: 700;
  color: var(--color-on-surface-variant);
}

.money-input .input {
  padding-left: 42px;
  font-size: 18px;
  font-weight: 700;
}

.formula {
  padding: 12px 14px;
  border-radius: var(--radius-sm);
  background: var(--color-primary-soft);
  color: var(--color-primary);
  font-size: 13px;
  overflow-x: auto;
}

.grow {
  flex: 1;
}

.range {
  width: 100%;
  accent-color: var(--color-primary);
  margin-top: 8px;
}

.preview {
  display: flex;
  gap: 12px;
  margin: 20px 0;
  flex-wrap: wrap;
}

.preview-main,
.preview-compare {
  flex: 1;
  min-width: 160px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 16px 18px;
  border-radius: var(--radius-md);
}

.preview-main {
  background: var(--color-primary);
  color: #ffffff;
}

.preview-main .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

:root[data-theme='dark'] .preview-main {
  color: var(--color-on-primary);
}

:root[data-theme='dark'] .preview-main .stat-label {
  color: var(--color-on-primary);
}

.preview-main strong {
  font-size: 32px;
  font-weight: 800;
}

.preview-compare {
  background: var(--color-surface-variant);
}

.preview-compare strong {
  font-size: 22px;
}

.compact td,
.compact th {
  padding: 8px 12px;
}

.accent {
  color: var(--color-primary);
  font-weight: 700;
}

@media (max-width: 960px) {
  .tariff-grid {
    grid-template-columns: 1fr;
  }
}
</style>
