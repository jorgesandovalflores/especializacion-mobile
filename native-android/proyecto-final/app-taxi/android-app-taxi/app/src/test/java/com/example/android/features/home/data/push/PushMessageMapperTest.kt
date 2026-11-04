package com.example.android.features.home.data.push

import com.example.android.features.home.domain.model.PushMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PushMessageMapperTest {

    @Test
    fun `lee titulo, cuerpo y deeplink del payload de datos`() {
        val data = mapOf(
            "title" to "Tu taxi está llegando",
            "body" to "Carlos llega en 3 min",
            "deeplink" to "app-taxi://passenger/menu"
        )

        assertEquals(
            PushMessage("Tu taxi está llegando", "Carlos llega en 3 min", "app-taxi://passenger/menu"),
            data.toPushMessage()
        )
    }

    @Test
    fun `sin deeplink abre Home`() {
        val message = mapOf("title" to "Hola", "body" to "Mundo").toPushMessage()

        assertEquals(DEEPLINK_HOME, message?.deeplink)
    }

    @Test
    fun `sin titulo o sin cuerpo se ignora`() {
        assertNull(mapOf("body" to "Mundo").toPushMessage())
        assertNull(mapOf("title" to "Hola", "body" to " ").toPushMessage())
    }
}
