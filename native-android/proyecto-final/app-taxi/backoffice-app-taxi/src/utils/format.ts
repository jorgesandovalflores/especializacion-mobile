import type { OfferStatus, TripStatus, UserStatus } from '@/types/models'

const dateTimeFormatter = new Intl.DateTimeFormat('es-PE', {
  dateStyle: 'medium',
  timeStyle: 'short'
})

const timeFormatter = new Intl.DateTimeFormat('es-PE', {
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit'
})

export function formatDateTime(value: string | null | undefined): string {
  if (!value) return '—'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '—' : dateTimeFormatter.format(date)
}

export function formatTime(value: string | null | undefined): string {
  if (!value) return '—'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '—' : timeFormatter.format(date)
}

export function round2(value: number): number {
  return Math.round((value + Number.EPSILON) * 100) / 100
}

export function formatMoney(value: number | null | undefined, currency = 'PEN'): string {
  if (value === null || value === undefined || Number.isNaN(value)) return '—'
  const symbol = currency === 'PEN' ? 'S/' : currency
  return `${symbol} ${value.toFixed(2)}`
}

export function formatDistance(meters: number | null | undefined): string {
  if (meters === null || meters === undefined) return '—'
  if (meters < 1000) return `${Math.round(meters)} m`
  return `${(meters / 1000).toFixed(1)} km`
}

export function formatDuration(seconds: number | null | undefined): string {
  if (seconds === null || seconds === undefined) return '—'
  const minutes = Math.max(1, Math.round(seconds / 60))
  if (minutes < 60) return `${minutes} min`
  const hours = Math.floor(minutes / 60)
  return `${hours} h ${minutes % 60} min`
}

export function fullName(person: { givenName?: string | null; familyName?: string | null } | null | undefined): string {
  if (!person) return '—'
  const name = [person.givenName, person.familyName].filter(Boolean).join(' ').trim()
  return name || 'Sin nombre'
}

export function formatPhone(phone: string | null | undefined): string {
  if (!phone) return '—'
  if (phone.startsWith('51') && phone.length === 11) {
    return `+51 ${phone.slice(2, 5)} ${phone.slice(5, 8)} ${phone.slice(8)}`
  }
  return phone
}

export const tripStatusLabels: Record<TripStatus, string> = {
  REQUESTED: 'Solicitado',
  ACCEPTED: 'Aceptado',
  ARRIVED: 'Conductor llegó',
  IN_PROGRESS: 'En curso',
  COMPLETED: 'Completado',
  CANCELLED: 'Cancelado'
}

export const offerStatusLabels: Record<OfferStatus, string> = {
  PENDING: 'Pendiente',
  ACCEPTED: 'Aceptada',
  REJECTED: 'Rechazada'
}

export const userStatusLabels: Record<UserStatus, string> = {
  ACTIVE: 'Activo',
  INACTIVE: 'Inactivo'
}

export function relativeTime(value: string | null | undefined, now = Date.now()): string {
  if (!value) return '—'
  const diff = Math.round((now - new Date(value).getTime()) / 1000)
  if (Number.isNaN(diff)) return '—'
  if (diff < 10) return 'ahora'
  if (diff < 60) return `hace ${diff} s`
  if (diff < 3600) return `hace ${Math.floor(diff / 60)} min`
  if (diff < 86400) return `hace ${Math.floor(diff / 3600)} h`
  return formatDateTime(value)
}
