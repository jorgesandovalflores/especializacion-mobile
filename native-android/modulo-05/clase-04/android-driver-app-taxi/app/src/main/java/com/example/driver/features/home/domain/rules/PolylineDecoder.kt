package com.example.driver.features.home.domain.rules

import com.example.driver.features.home.domain.model.GeoPoint
import kotlin.math.pow

object PolylineDecoder {

    fun decode(encoded: String, precision: Int = 5): List<GeoPoint> {
        val factor = 10.0.pow(precision)
        val points = mutableListOf<GeoPoint>()
        var index = 0
        var lat = 0L
        var lng = 0L
        while (index < encoded.length) {
            val dLat = readValue(encoded, index) ?: break
            index = dLat.second
            val dLng = readValue(encoded, index) ?: break
            index = dLng.second
            lat += dLat.first
            lng += dLng.first
            points += GeoPoint(lat / factor, lng / factor)
        }
        return points
    }

    private fun readValue(encoded: String, start: Int): Pair<Long, Int>? {
        var index = start
        var result = 0L
        var shift = 0
        while (true) {
            if (index >= encoded.length) return null
            val chunk = encoded[index++].code - ASCII_OFFSET
            result = result or ((chunk and CHUNK_MASK).toLong() shl shift)
            shift += CHUNK_BITS
            if (chunk < CONTINUATION_BIT) break
        }
        val value = if (result and 1L != 0L) (result shr 1).inv() else result shr 1
        return value to index
    }

    private const val ASCII_OFFSET = 63
    private const val CHUNK_MASK = 0x1f
    private const val CHUNK_BITS = 5
    private const val CONTINUATION_BIT = 0x20
}
