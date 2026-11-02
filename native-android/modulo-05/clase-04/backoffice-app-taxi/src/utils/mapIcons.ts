import L from 'leaflet'

export type DriverMarkerState = 'free' | 'busy' | 'offline'

const CAR_SVG =
  '<svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><path d="M5 11l1.6-4.2A2 2 0 0 1 8.5 5.5h7a2 2 0 0 1 1.9 1.3L19 11h.5A1.5 1.5 0 0 1 21 12.5V17a1 1 0 0 1-1 1h-1a2 2 0 0 1-4 0H9a2 2 0 0 1-4 0H4a1 1 0 0 1-1-1v-4.5A1.5 1.5 0 0 1 4.5 11zm2.2 0h9.6l-1.2-3.3a.5.5 0 0 0-.5-.3H8.9a.5.5 0 0 0-.5.3z"/></svg>'

export function driverIcon(state: DriverMarkerState, bearing: number | null, selected = false): L.DivIcon {
  const heading =
    bearing !== null && bearing !== undefined && state !== 'offline'
      ? `<span class="heading" style="transform: rotate(${Math.round(bearing)}deg)"></span>`
      : ''
  return L.divIcon({
    className: 'map-pin',
    html: `<div class="driver-pin ${state}${selected ? ' selected' : ''}">${heading}${CAR_SVG}</div>`,
    iconSize: [30, 30],
    iconAnchor: [15, 15],
    tooltipAnchor: [0, -18]
  })
}

export function placeIcon(kind: 'origin' | 'destination'): L.DivIcon {
  return L.divIcon({
    className: 'map-pin',
    html: `<div class="place-pin ${kind}"><span>${kind === 'origin' ? 'A' : 'B'}</span></div>`,
    iconSize: [30, 30],
    iconAnchor: [15, 34],
    tooltipAnchor: [0, -34]
  })
}

export function driverState(online: boolean, tripId: string | null): DriverMarkerState {
  if (tripId) return 'busy'
  return online ? 'free' : 'offline'
}

export const driverStateLabels: Record<DriverMarkerState, string> = {
  free: 'Libre',
  busy: 'Ocupado',
  offline: 'Fuera de labores'
}

export const ROUTE_COLOR = '#7843E6'
export const TRACK_COLOR = '#00B8D4'
