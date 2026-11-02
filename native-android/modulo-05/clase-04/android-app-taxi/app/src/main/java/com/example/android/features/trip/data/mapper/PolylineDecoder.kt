package com.example.android.features.trip.data.mapper

import com.example.android.features.trip.domain.model.GeoPoint

object PolylineDecoder {

    private const val PRECISION = 1e5
    private const val CHUNK_MASK = 0x1f
    private const val CONTINUATION = 0x20
    private const val ASCII_OFFSET = 63

    fun decode(encoded: String): List<GeoPoint> {
        val points = mutableListOf<GeoPoint>()
        var index = 0
        var lat = 0
        var lng = 0
        while (index < encoded.length) {
            val (dLat, afterLat) = nextValue(encoded, index)
            val (dLng, afterLng) = nextValue(encoded, afterLat)
            if (afterLng > encoded.length) break
            lat += dLat
            lng += dLng
            index = afterLng
            points += GeoPoint(lat / PRECISION, lng / PRECISION)
        }
        return points
    }

    private fun nextValue(encoded: String, start: Int): Pair<Int, Int> {
        var result = 0
        var shift = 0
        var index = start
        var chunk: Int
        do {
            if (index >= encoded.length) return 0 to encoded.length + 1
            chunk = encoded[index++].code - ASCII_OFFSET
            result = result or ((chunk and CHUNK_MASK) shl shift)
            shift += 5
        } while (chunk >= CONTINUATION)
        val value = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        return value to index
    }
}
