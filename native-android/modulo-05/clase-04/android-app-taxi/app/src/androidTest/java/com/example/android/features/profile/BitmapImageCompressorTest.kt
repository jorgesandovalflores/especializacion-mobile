package com.example.android.features.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android.features.profile.data.storage.BitmapImageCompressor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class BitmapImageCompressorTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun compress_resizesLongestSideTo1080AndReturnsJpeg(): Unit = runBlocking {
        val file = File(context.cacheDir, "foto-grande.png")
        Bitmap.createBitmap(3000, 2000, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.MAGENTA)
            file.outputStream().use { compress(Bitmap.CompressFormat.PNG, 100, it) }
            recycle()
        }

        val bytes = BitmapImageCompressor(context.contentResolver).compress(Uri.fromFile(file).toString())

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        assertEquals("image/jpeg", bounds.outMimeType)
        assertEquals(1080, bounds.outWidth)
        assertEquals(720, bounds.outHeight)
        assertTrue(bytes.size < file.length())
        file.delete()
    }
}
