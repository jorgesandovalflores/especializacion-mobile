package com.example.android.features.profile.data.storage

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.core.net.toUri
import com.example.android.features.profile.domain.repository.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

class BitmapImageCompressor(
    private val resolver: ContentResolver
) : ImageCompressor {

    override suspend fun compress(uri: String): ByteArray = withContext(Dispatchers.IO) {
        val source = ImageDecoder.createSource(resolver, uri.toUri())
        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val longest = max(info.size.width, info.size.height)
            if (longest > MAX_SIDE_PX) {
                val scale = MAX_SIDE_PX.toFloat() / longest
                decoder.setTargetSize(
                    (info.size.width * scale).roundToInt(),
                    (info.size.height * scale).roundToInt()
                )
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            bitmap.recycle()
            out.toByteArray()
        }
    }

    private companion object {
        const val MAX_SIDE_PX = 1080
        const val JPEG_QUALITY = 85
    }
}
