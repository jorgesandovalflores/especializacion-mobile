# Módulo 3 · Sesión 2 — SharedPreferences (historia, práctica y alternativa moderna con DataStore)

---

## Objetivos de aprendizaje

1. Comprender la **evolución histórica** de SharedPreferences y por qué nace **DataStore**.
2. Aplicar **SharedPreferences** correctamente para datos simples, evitando ANRs y malas prácticas.
3. Implementar **DataStore (Preferences)** con Flow como alternativa moderna y reactiva.
4. Proteger información sensible con **EncryptedSharedPreferences** y conocer el enfoque con **Proto DataStore**.
5. Construir un **formulario de registro en 2 pasos** que persista el progreso (borrador) de forma segura, migrando de SharedPreferences a DataStore si es necesario.

---

## Contenido de la clase

1. **Evolución histórica**: SharedPreferences → Preference Support → Jetpack Security → DataStore (Preferences/Proto).
2. **Fundamentos de SharedPreferences** (API, `apply()` vs `commit()`, antipatrones).
3. **DataStore Preferences**: claves tipadas, Flow, migraciones desde SharedPreferences.
4. **Seguridad**: EncryptedSharedPreferences, consideraciones para contraseñas/tokens, enfoque con Proto DataStore.
5. **Caso práctico**: Registro de usuario en 2 pasos con persistencia de borrador, validación y limpieza final.
6. **Buenas prácticas y checklist de producción**.

---

## 1) Evolución histórica (por qué hemos cambiado con el tiempo)

En Android, la persistencia ligera de datos (configuraciones, flags, sesión, etc.) ha pasado por varias etapas. Cada evolución respondió a **limitaciones del modelo anterior**, buscando mayor **seguridad, estabilidad y reactividad** en un entorno cada vez más exigente.

---

### **Android 1.x–2.x: SharedPreferences clásico**

**Contexto:**  
Fue introducido como una solución rápida para almacenar información pequeña, sin necesidad de usar bases de datos.  
Internamente escribía y leía de un **archivo XML** almacenado en el sistema de archivos privado de la app.

**Características:**

-   API simple basada en clave–valor (`putString`, `putBoolean`, etc.).
-   Almacenamiento local en XML (`/data/data/<paquete>/shared_prefs/`).
-   Acceso con `getSharedPreferences(name, MODE_PRIVATE)` o `PreferenceManager`.
-   Métodos de escritura:
    -   `commit()` → sincrónico, devuelve `boolean` (puede bloquear el hilo principal).
    -   `apply()` (añadido en API 9) → asíncrono, no bloquea la UI.

**Problemas:**

-   Riesgo de **ANRs** si se usan escrituras frecuentes con `commit()`.
-   No soporta **lecturas concurrentes seguras**.
-   No es **reactivo** (no puede notificar automáticamente cambios).
-   Los datos se almacenan en **texto plano**, sin cifrado.

> En esta etapa, SharedPreferences era suficiente para apps simples, pero no para las más modernas o concurrentes.

---

### **2013–2017: Preference Support / Compat**

**Contexto:**  
Con la expansión de Android y el auge de las apps con múltiples configuraciones, Google lanzó `PreferenceFragmentCompat` dentro de la librería de soporte (`androidx.preference`).

**Características:**

-   Introdujo un framework visual para crear **pantallas de ajustes (Settings)**.
-   Mantenía la misma base: **SharedPreferences** como backend.
-   Permitía persistir automáticamente valores desde componentes de UI (`SwitchPreference`, `EditTextPreference`, etc.).

**Limitaciones:**

-   Mismo problema estructural: **acceso sincrónico** a un archivo XML.
-   Persistencia débil ante **cierres abruptos** o **escrituras concurrentes**.
-   Falta de control granular sobre hilos y transacciones.

> Aunque mejoró la experiencia de configuración, el motor seguía siendo SharedPreferences: inseguro, no thread-safe y poco eficiente para apps grandes.

---

### **2019+: Jetpack Security (EncryptedSharedPreferences)**

**Contexto:**  
La necesidad de **proteger información sensible** (tokens, contraseñas, preferencias personales) llevó a Google a introducir la librería **Jetpack Security Crypto**.  
Esta librería permite cifrar automáticamente los datos de SharedPreferences con claves gestionadas por el **Android Keystore**.

**Características:**

-   Basado en `EncryptedSharedPreferences` y `MasterKey`.
-   Cifrado simétrico (AES-256-GCM y AES-256-SIV).
-   Compatible con código existente (API idéntica a SharedPreferences).
-   Las claves se guardan en el **Keystore del sistema**.

**Limitaciones:**

-   No soluciona los problemas de **concurrencia** ni **bloqueo en UI**.
-   Sigue usando **XML** y escritura secuencial.
-   No es reactivo ni pensado para arquitectura basada en Flow o coroutines.

> Soluciona el “qué tan seguro” es almacenar datos, pero no el “cómo” ni el “cuándo” se escriben o leen.

---

### **2020+: Jetpack DataStore (Preferences / Proto)**

**Contexto:**  
Google desarrolla DataStore como el **reemplazo oficial** de SharedPreferences.  
Su arquitectura está basada en **Kotlin Coroutines + Flow**, eliminando bloqueos y errores de concurrencia.

**Características:**

-   Almacenamiento binario, no XML.
-   Thread-safe, no bloquea el main thread.
-   Escrituras **atómicas y consistentes**.
-   API reactiva (Flow): cada cambio puede observarse automáticamente.
-   Dos modos:
    -   **Preferences DataStore:** clave–valor simple (sustituye directamente SharedPreferences).
    -   **Proto DataStore:** usa mensajes tipados con `.proto` (permite estructuras complejas + cifrado).
-   Soporte de **migraciones automáticas** desde SharedPreferences.

**Ventajas:**

-   Compatible con arquitecturas modernas (MVVM, Flow, Hilt).
-   Integración nativa con coroutines (`suspend`, `edit`, `map`).
-   Sin riesgos de ANR.
-   Mejor integración con pruebas unitarias.

> DataStore es la evolución natural: una capa moderna, reactiva y segura para el almacenamiento ligero.

---

## 2) SharedPreferences — fundamentos y antipatrones

```kotlin
class SessionPrefs(private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_DRAFT_NAME = "draft_name"
        private const val KEY_DRAFT_LASTNAME = "draft_lastname"
        private const val KEY_DRAFT_PHONE = "draft_phone"
        private const val KEY_DRAFT_EMAIL = "draft_email"
        private const val KEY_DRAFT_USERNAME = "draft_username"
    }

    fun savePersonalDraft(name: String, lastName: String, phone: String) {
        prefs.edit()
            .putString(KEY_DRAFT_NAME, name)
            .putString(KEY_DRAFT_LASTNAME, lastName)
            .putString(KEY_DRAFT_PHONE, phone)
            .apply()
    }

    fun saveAccountDraft(email: String, username: String) {
        prefs.edit()
            .putString(KEY_DRAFT_EMAIL, email)
            .putString(KEY_DRAFT_USERNAME, username)
            .apply()
    }

    fun clearDraft() = prefs.edit().clear().apply()
}
```

**Errores comunes:**

-   Usar `commit()` en UI thread → puede causar ANR.
-   Guardar objetos grandes → usar Room.
-   Acceso global sin wrapper → difícil de mantener.

---

## 3) DataStore (Preferences) — alternativa moderna

**Ventajas:** Thread-safe, Flow reactivo, migración desde SharedPreferences, sin ANR.

```kotlin
val Context.registrationDataStore by preferencesDataStore(
    name = "registration_prefs",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "session_prefs"))
    }
)
```

```kotlin
class RegistrationDataStore(private val context: Context) {

    private object Keys {
        val NAME = stringPreferencesKey("draft_name")
        val LASTNAME = stringPreferencesKey("draft_lastname")
        val PHONE = stringPreferencesKey("draft_phone")
        val EMAIL = stringPreferencesKey("draft_email")
        val USERNAME = stringPreferencesKey("draft_username")
    }

    suspend fun savePersonal(d: PersonalDraft) {
        context.registrationDataStore.edit { p ->
            p[Keys.NAME] = d.name
            p[Keys.LASTNAME] = d.lastName
            p[Keys.PHONE] = d.phone
        }
    }

    suspend fun saveAccount(d: AccountDraft) {
        context.registrationDataStore.edit { p ->
            p[Keys.EMAIL] = d.email
            p[Keys.USERNAME] = d.username
        }
    }

    suspend fun clear() { context.registrationDataStore.edit { it.clear() } }
}

data class PersonalDraft(val name: String, val lastName: String, val phone: String)
data class AccountDraft(val email: String, val username: String, val password: String = "")
```

---

## 4) Seguridad — EncryptedSharedPreferences y opciones con DataStore

```kotlin
class SecurePrefs(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_DRAFT_PASSWORD = "draft_password"
    }

    fun savePassword(password: String) {
        securePrefs.edit().putString(KEY_DRAFT_PASSWORD, password).apply()
    }

    fun getPassword(): String = securePrefs.getString(KEY_DRAFT_PASSWORD, "") ?: ""

    fun clear() = securePrefs.edit().clear().apply()
}
```

> **Proto DataStore** permite cifrado personalizado en `Serializer` para escenarios avanzados.

---

## 5) Caso práctico: Registro de usuario en 2 pasos

**Objetivo:** guardar progreso del registro en 2 pantallas, restaurar si la app se cierra.

```kotlin
@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val ds: RegistrationDataStore,
    private val secure: SecurePrefs
) : ViewModel() {

    val personal: StateFlow<PersonalDraft> = ds.personalFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PersonalDraft("", "", ""))

    val account: StateFlow<AccountDraft> = ds.accountFlow
        .map { it.copy(password = secure.getPassword()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountDraft("", "", ""))

    fun savePersonal(name: String, last: String, phone: String) = viewModelScope.launch {
        ds.savePersonal(PersonalDraft(name, last, phone))
    }

    fun saveAccount(email: String, user: String, password: String) = viewModelScope.launch {
        ds.saveAccount(AccountDraft(email, user))
        secure.savePassword(password)
    }

    fun submitRegistration(onSuccess: () -> Unit, onError: (Throwable) -> Unit) =
        viewModelScope.launch {
            try {
                ds.clear(); secure.clear(); onSuccess()
            } catch (t: Throwable) { onError(t) }
        }
}
```

### UI (Compose)

```kotlin
@Composable
fun StepPersonalScreen(vm: RegistrationViewModel, onNext: () -> Unit) {
    val state = vm.personal.collectAsState()
    var name by remember { mutableStateOf(state.value.name) }
    var last by remember { mutableStateOf(state.value.lastName) }
    var phone by remember { mutableStateOf(state.value.phone) }

    Column {
        TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        TextField(value = last, onValueChange = { last = it }, label = { Text("Last name") })
        TextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
        Button(onClick = { vm.savePersonal(name, last, phone); onNext() }) { Text("Continue") }
    }
}
```

---

## 6) Buenas prácticas

-   Usar **DataStore** para flags y preferencias pequeñas.
-   Usar **EncryptedSharedPreferences** o **Proto DS cifrado** para contraseñas/tokens.
-   Migrar con **SharedPreferencesMigration**.
-   Centralizar acceso a datos en wrappers o repositorios.
-   Limpiar datos sensibles tras éxito.
-   Revisar **Auto Backup** si se manejan datos sensibles.
