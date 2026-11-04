<script setup lang="ts">
import { computed } from 'vue'
import { offerStatusLabels, tripStatusLabels, userStatusLabels } from '@/utils/format'
import type { OfferStatus, TripStatus, UserStatus } from '@/types/models'

type Props =
  | { type: 'trip'; value: TripStatus }
  | { type: 'user'; value: UserStatus }
  | { type: 'offer'; value: OfferStatus }
  | { type: 'online'; value: boolean }

const props = defineProps<Props>()

const tripTone: Record<TripStatus, string> = {
  REQUESTED: 'warning',
  ACCEPTED: 'info',
  ARRIVED: 'info',
  IN_PROGRESS: 'primary',
  COMPLETED: 'success',
  CANCELLED: 'error'
}

const offerTone: Record<OfferStatus, string> = {
  PENDING: 'warning',
  ACCEPTED: 'success',
  REJECTED: ''
}

const view = computed(() => {
  switch (props.type) {
    case 'trip':
      return { label: tripStatusLabels[props.value], tone: tripTone[props.value] }
    case 'user':
      return { label: userStatusLabels[props.value], tone: props.value === 'ACTIVE' ? 'success' : '' }
    case 'offer':
      return { label: offerStatusLabels[props.value], tone: offerTone[props.value] }
    default:
      return { label: props.value ? 'En labores' : 'Desconectado', tone: props.value ? 'success' : '' }
  }
})
</script>

<template>
  <span class="badge" :class="view.tone">{{ view.label }}</span>
</template>
