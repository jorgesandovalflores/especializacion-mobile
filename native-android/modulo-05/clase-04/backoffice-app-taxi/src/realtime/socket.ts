import { io, type Socket } from 'socket.io-client'
import { API_URL } from '@/api/config'
import type { DriverAvailabilityEvent, DriverLocation, TripDto } from '@/types/models'

export interface SessionReadyEvent {
  userType: string
  userId: string
}

export interface RealtimeEvents {
  'session:ready': SessionReadyEvent
  'driver:location': DriverLocation
  'driver:availability': DriverAvailabilityEvent
  'trip:updated': TripDto
}

export type RealtimeEventName = keyof RealtimeEvents

export type RealtimeHandler<E extends RealtimeEventName> = (payload: RealtimeEvents[E]) => void

export type ConnectionState = 'idle' | 'connecting' | 'connected' | 'reconnecting' | 'error'

export interface RealtimeCallbacks {
  onState: (state: ConnectionState) => void
  onUnauthorized: () => void
}

const EVENTS: RealtimeEventName[] = ['session:ready', 'driver:location', 'driver:availability', 'trip:updated']

const handlers: { [E in RealtimeEventName]: Set<RealtimeHandler<E>> } = {
  'session:ready': new Set(),
  'driver:location': new Set(),
  'driver:availability': new Set(),
  'trip:updated': new Set()
}

let socket: Socket | null = null

function dispatch<E extends RealtimeEventName>(event: E, payload: RealtimeEvents[E]) {
  handlers[event].forEach((handler) => handler(payload))
}

export function connectRealtime(token: string, callbacks: RealtimeCallbacks) {
  disconnectRealtime()
  callbacks.onState('connecting')
  const instance = io(`${API_URL}/realtime`, {
    auth: { token },
    transports: ['websocket'],
    reconnection: true,
    reconnectionDelay: 1500,
    reconnectionDelayMax: 8000
  })

  instance.on('connect', () => callbacks.onState('connected'))
  instance.on('disconnect', (reason) => {
    callbacks.onState(reason === 'io client disconnect' ? 'idle' : 'reconnecting')
  })
  instance.on('connect_error', (error) => {
    if (error.message === 'unauthorized') {
      instance.disconnect()
      callbacks.onState('error')
      callbacks.onUnauthorized()
      return
    }
    callbacks.onState(instance.active ? 'reconnecting' : 'error')
  })

  for (const event of EVENTS) {
    instance.on(event, (payload: RealtimeEvents[typeof event]) => dispatch(event, payload))
  }

  socket = instance
}

export function disconnectRealtime() {
  if (!socket) return
  socket.removeAllListeners()
  socket.disconnect()
  socket = null
}

export function subscribe<E extends RealtimeEventName>(event: E, handler: RealtimeHandler<E>): () => void {
  handlers[event].add(handler)
  return () => {
    handlers[event].delete(handler)
  }
}
