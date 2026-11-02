package com.example.android.features.trip.data.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.example.android.features.trip.domain.model.GeoPoint
import com.example.android.features.trip.domain.repository.AddressResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

class GeocoderAddressResolver(private val context: Context) : AddressResolver {

    override suspend fun addressOf(point: GeoPoint): String? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.getDefault())
        val address = withTimeoutOrNull(TIMEOUT_MS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(point.lat, point.lng, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            continuation.resume(addresses.firstOrNull())
                        }

                        override fun onError(errorMessage: String?) {
                            continuation.resume(null)
                        }
                    })
                }
            } else {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    runCatching { geocoder.getFromLocation(point.lat, point.lng, 1)?.firstOrNull() }.getOrNull()
                }
            }
        }
        return address?.let { listOfNotNull(it.thoroughfare, it.subThoroughfare).joinToString(" ").ifBlank { it.getAddressLine(0) } }
    }

    private companion object {
        const val TIMEOUT_MS = 3_000L
    }
}
