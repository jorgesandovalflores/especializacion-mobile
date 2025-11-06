# Firebase Cloud Messaging (FCM) y Push Silenciosos — Android/Kotlin

## 1. Qué es Firebase Cloud Messaging (FCM)

Firebase Cloud Messaging (FCM) es el sistema de mensajería push de Google que permite enviar mensajes desde el backend o Firebase Console a dispositivos Android, iOS o web.

### Tipos de mensajes en FCM

| Tipo | Entregado a | Procesado por | Uso principal |
|------|--------------|----------------|----------------|
| **Notification Message** | `system tray` | Android OS | Mostrar una notificación visible |
| **Data Message** | `app code` | Tu app (en background/foreground) | Lógica personalizada |
| **Mixed (Notification + Data)** | Ambos | OS + app | Notificación visible con datos adjuntos |

---

## 2. Arquitectura general

1. Tu app se registra con **FirebaseMessagingService** → obtiene un **registration token**.
2. El backend guarda ese token.
3. Envías mensajes desde tu servidor a la API de FCM.
4. FCM enruta el mensaje al dispositivo, incluso si la app está cerrada.

---

## 3. Notificaciones visibles (Notification Message)

```json
{
  "message": {
    "token": "DEVICE_TOKEN",
    "notification": {
      "title": "Nueva carrera disponible",
      "body": "Un pasajero está cerca de tu zona"
    },
    "android": {
      "notification": {
        "channel_id": "general_channel",
        "priority": "HIGH"
      }
    }
  }
}
```

Si la app está **en foreground**, puedes interceptarla y mostrar tu propia notificación.

---

## 4. Mensajes silenciosos (Data Message)

Estos no generan notificación visible y se usan para actualizar datos o disparar procesos en segundo plano.

```json
{
  "message": {
    "token": "DEVICE_TOKEN",
    "data": {
      "action": "sync_trips",
      "timestamp": "1730834200"
    }
  }
}
```

```kotlin
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        when (data["action"]) {
            "sync_trips" -> syncTrips()
            "refresh_token" -> refreshAuth()
        }
    }
}
```

---

## 5. Push Silenciosos (Silent Push)

Mensajes data con prioridad alta que ejecutan lógica sin mostrar UI.

```json
{
  "message": {
    "token": "DEVICE_TOKEN",
    "data": { "event": "update_location" },
    "android": { "priority": "HIGH" }
  }
}
```

### Usos comunes
- Actualizar estado del viaje.
- Sincronizar data de fondo.
- Refrescar token de autenticación.

### Restricciones
- En Android 13+ se requiere permiso `POST_NOTIFICATIONS`.
- Si el usuario desactiva notificaciones, también se bloquean los push silenciosos.
- En modo *Doze*, se pueden retrasar salvo que uses prioridad alta.

---

## 6. Prioridades y límites

| Prioridad | Uso | Efecto |
|------------|-----|--------|
| **High** | mensajes urgentes | Despierta CPU y entrega inmediata |
| **Normal** | sincronizaciones pasivas | Entrega diferida |

Recomendado usar **mix** de prioridades según criticidad.

---

## 7. Restricciones de background (Android 8+)

Desde Android Oreo (API 26):
- Restricciones para servicios en background.
- Si se requiere ejecución prolongada, usar:
  - **WorkManager**
  - **Foreground Service**

Ejemplo: mensaje "update_location" → inicia un `ForegroundService` con notificación visible.

---

## 8. Buenas prácticas

- Usar `priority: "high"` para push silenciosos críticos.
- Agregar `ttl` (time to live) en segundos: `"ttl": "30s"`.
- Payload máximo 4 KB.
- Implementar reintentos desde backend.
- Mantener coherencia con caché local.

---

## 9. Canales de notificación (Android 8+)

Requieren `NotificationChannel`:
- `IMPORTANCE_HIGH` → notificación emergente.
- `IMPORTANCE_LOW` → silenciosa.

Los canales se crean una sola vez y no pueden cambiar su importancia.

---

## 10. Envío desde backend (Node.js/NestJS)

```js
import fetch from "node-fetch"

await fetch("https://fcm.googleapis.com/v1/projects/YOUR_PROJECT/messages:send", {
  method: "POST",
  headers: {
    "Authorization": `Bearer ${accessToken}`,
    "Content-Type": "application/json"
  },
  body: JSON.stringify({
    message: {
      token: deviceToken,
      data: { event: "refresh_location" },
      android: { priority: "HIGH" }
    }
  })
})
```

El `accessToken` se obtiene con las **credenciales del servicio (serviceAccount)**.

---

## 11. Resumen general

- Usa **Notification Messages** para avisos al usuario.
- Usa **Data Messages** para sincronización o lógica interna.
- Los **Push silenciosos** son Data Messages de alta prioridad.
- Considera restricciones de energía y permisos en Android 13+.
- En tareas críticas, combina FCM + WorkManager o Foreground Service.
