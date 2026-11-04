import L from 'leaflet'
import { onBeforeUnmount, onMounted, shallowRef, type Ref } from 'vue'

export const LIMA_CENTER: L.LatLngTuple = [-12.0464, -77.0428]

export function useLeafletMap(container: Ref<HTMLElement | null>, zoom = 12) {
  const map = shallowRef<L.Map | null>(null)
  let observer: ResizeObserver | null = null

  onMounted(() => {
    if (!container.value) return
    const instance = L.map(container.value, {
      center: LIMA_CENTER,
      zoom,
      zoomControl: true,
      attributionControl: true
    })
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      subdomains: ['a', 'b', 'c'],
      attribution: '&copy; colaboradores de <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(instance)
    observer = new ResizeObserver(() => instance.invalidateSize())
    observer.observe(container.value)
    map.value = instance
  })

  onBeforeUnmount(() => {
    observer?.disconnect()
    map.value?.remove()
    map.value = null
  })

  function fitTo(points: L.LatLngExpression[], maxZoom = 16) {
    if (!map.value || points.length === 0) return
    if (points.length === 1) {
      map.value.setView(points[0], Math.min(maxZoom, 15))
      return
    }
    map.value.fitBounds(L.latLngBounds(points), { padding: [48, 48], maxZoom })
  }

  return { map, fitTo }
}
