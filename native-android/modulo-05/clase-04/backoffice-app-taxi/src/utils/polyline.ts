export type LatLngTuple = [number, number]

export function decodePolyline(encoded: string | null | undefined, precision = 5): LatLngTuple[] {
  if (!encoded) return []
  const factor = Math.pow(10, precision)
  const points: LatLngTuple[] = []
  let index = 0
  let lat = 0
  let lng = 0

  const readValue = (): number | null => {
    let result = 0
    let shift = 0
    let byte: number
    do {
      if (index >= encoded.length) return null
      byte = encoded.charCodeAt(index++) - 63
      result |= (byte & 0x1f) << shift
      shift += 5
    } while (byte >= 0x20)
    return result & 1 ? ~(result >> 1) : result >> 1
  }

  while (index < encoded.length) {
    const dLat = readValue()
    const dLng = readValue()
    if (dLat === null || dLng === null) break
    lat += dLat
    lng += dLng
    points.push([lat / factor, lng / factor])
  }
  return points
}

export function encodePolyline(points: LatLngTuple[], precision = 5): string {
  const factor = Math.pow(10, precision)
  let output = ''
  let prevLat = 0
  let prevLng = 0

  const writeValue = (value: number) => {
    let v = value < 0 ? ~(value << 1) : value << 1
    while (v >= 0x20) {
      output += String.fromCharCode((0x20 | (v & 0x1f)) + 63)
      v >>= 5
    }
    output += String.fromCharCode(v + 63)
  }

  for (const [lat, lng] of points) {
    const iLat = Math.round(lat * factor)
    const iLng = Math.round(lng * factor)
    writeValue(iLat - prevLat)
    writeValue(iLng - prevLng)
    prevLat = iLat
    prevLng = iLng
  }
  return output
}
