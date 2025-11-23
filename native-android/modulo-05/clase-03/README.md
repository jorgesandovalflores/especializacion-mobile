# Módulo 05 · Sesión 03

# Testing con JUnit y Compose Test

## Objetivos de la sesión

Al finalizar esta sesión, el estudiante será capaz de:

1. Entender por qué es crítico hacer testing en apps móviles reales.
2. Diferenciar **pruebas unitarias**, **pruebas de UI** y **pruebas instrumentadas** en Android.
3. Aplicar **mocks** y **fakes** para desacoplar el código de dependencias externas.
4. Escribir pruebas reales para el flujo de OTP de la pantalla `SignInGenerateOtpScreen`.

---

## Contenido de la sesión

1. Introducción: por qué hacer testing
2. Tipos de pruebas en Android
    - Pruebas unitarias
    - Pruebas de UI
    - Pruebas instrumentadas
3. Mocks y fakes
4. Ejemplos paso a paso usando `SignInGenerateOtpScreen`

---

## 1) Introducción: por qué hacer testing

Piensa en tu flujo de OTP:

-   El usuario ingresa su teléfono.
-   El sistema genera un código.
-   Navegas a la pantalla de validación.

Si algo falla aquí:

-   Usuarios quedan bloqueados del sistema.
-   El soporte se llena de reclamos.
-   Es difícil refactorizar sin miedo.

Testing te ayuda a:

1. **Prevenir regresiones**  
   Cuando cambias el `OtpGenerateUseCase` o el `AuthRepositoryImpl`, los tests te avisan si rompiste algo.

2. **Documentar el comportamiento esperado**  
   Un test como  
   `cuando el teléfono tiene 9 dígitos, el botón se habilita`  
   es una especificación ejecutable.

3. **Refactorizar con confianza**  
   Puedes cambiar internamente tu lógica (por ejemplo, cómo manejas errores con `ErrorMapper`) sabiendo que, mientras los tests pasen, el comportamiento “contractual” se mantiene.

4. **Medir calidad de una funcionalidad crítica**  
   Login y OTP siempre son flujos de alto impacto. Son candidatos naturales para tener buena cobertura.

---

## 2) Tipos de pruebas en Android

En la práctica Android maneja dos grandes carpetas de tests:

-   `src/test/java` → pruebas unitarias (JVM local).
-   `src/androidTest/java` → pruebas instrumentadas (en dispositivo/emulador, incluyendo UI).

Para efectos didácticos, separamos en tres tipos:

1. Pruebas unitarias
2. Pruebas de UI (Compose/Espresso)
3. Pruebas instrumentadas (integración con Android Framework)

### 2.1 Visión general

| Tipo de prueba        | Dónde corre                 | Qué prueba principalmente                         |
| --------------------- | --------------------------- | ------------------------------------------------- |
| Unitaria              | JVM local (`test`)          | Lógica pura: use cases, validadores, mapeos, etc. |
| UI (Compose/Espresso) | Dispositivo (`androidTest`) | Componentes de UI, interacción usuario.           |
| Instrumentada         | Dispositivo (`androidTest`) | Integración con Android (Context, permisos, etc.) |

---

## 3) Contexto: qué vamos a testear

Tu flujo de OTP está compuesto por:

-   `SignInGenerateOtpRoute`:

    -   Orquesta permisos.
    -   Llama al `ViewModel` (`vm.callGenerateOtp`).
    -   Navega cuando `OtpGenerateState` es `Success`.

-   `SignInGenerateOtpScreen`:

    -   Maneja la UI: texto, campo de teléfono, botón “Ingresar”, `BaseToast`.
    -   Aplica accesibilidad (TalkBack).

-   `OtpGenerateUseCase`:

    -   Arma el `phone` con prefijo `"51"`.
    -   Emite `Loading`, `Success` o `Error`.

-   `AuthRepositoryImpl`:
    -   Llama a `AuthApi`.
    -   Mapea errores usando `ErrorMapper`.

Vamos a usar este mismo flujo para ilustrar cada tipo de prueba.

---

## 4) Paso a paso

### Paso 1: Configurar dependencias de testing

```kotlin
dependencies {
    // Pruebas unitarias (JVM local)
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    // Pruebas instrumentadas
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Compose Testing
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // (Opcional) librería de mocks, por ejemplo MockK
    testImplementation("io.mockk:mockk:1.13.12")
}
```

---

### Paso 2: Pruebas unitarias – `OtpGenerateUseCase`

#### 2.1 FakeAuthRepository

```kotlin
class FakeAuthRepository(
    private val shouldFail: Boolean = false
) : AuthRepository {

    override suspend fun otpGenerate(phone: String): OtpGenerateResult {
        if (shouldFail) {
            throw RuntimeException("Error de red")
        }
        return OtpGenerateResult(
            expiresAt = "2025-12-31T23:59:59Z"
        )
    }

    override suspend fun otpValidate(phone: String, code: String, tokenFcm: String): AuthResult {
        throw NotImplementedError("No se usa en estos tests")
    }
}
```

#### 2.2 Test unitario del use case

```kotlin
class OtpGenerateUseCaseTest {

    @Test
    fun `cuando el repo responde ok se emite Loading y luego Success`() = runTest {
        val fakeRepo = FakeAuthRepository(shouldFail = false)
        val useCase = OtpGenerateUseCase(repo = fakeRepo)

        val states = useCase("987654321").toList()

        assertTrue(states[0] is OtpGenerateState.Loading)

        val success = states[1] as OtpGenerateState.Success
        assertEquals("987654321", success.phone)
    }

    @Test
    fun `cuando el repo lanza error se emite Loading y luego Error`() = runTest {
        val fakeRepo = FakeAuthRepository(shouldFail = true)
        val useCase = OtpGenerateUseCase(repo = fakeRepo)

        val states = useCase("987654321").toList()

        assertTrue(states[0] is OtpGenerateState.Loading)
        val error = states[1] as OtpGenerateState.Error
        assertTrue(error.message.isNotBlank())
    }
}
```

---

### Paso 3: Pruebas de UI con Compose Testing – `SignInGenerateOtpScreen`

#### 3.1 Habilitar/deshabilitar botón según teléfono válido

```kotlin
@RunWith(AndroidJUnit4::class)
class SignInGenerateOtpScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `boton Ingresar debe estar desactivado cuando el telefono es invalido`() {
        composeRule.setContent {
            AndroidTheme(darkTheme = false) {
                SignInGenerateOtpScreen(
                    phone = "",
                    onPhoneChange = {},
                    isValid = false,
                    loading = false,
                    state = OtpGenerateState.Idle,
                    onSubmit = {}
                )
            }
        }

        composeRule
            .onNodeWithText("Ingresar")
            .assertIsNotEnabled()
    }

    @Test
    fun `boton Ingresar debe estar habilitado cuando el telefono es valido`() {
        composeRule.setContent {
            AndroidTheme(darkTheme = false) {
                SignInGenerateOtpScreen(
                    phone = "987654321",
                    onPhoneChange = {},
                    isValid = true,
                    loading = false,
                    state = OtpGenerateState.Idle,
                    onSubmit = {}
                )
            }
        }

        composeRule
            .onNodeWithText("Ingresar")
            .assertIsEnabled()
    }

    @Test
    fun `cuando el estado es Error se muestra el toast de error`() {
        val errorMessage = "No pudimos generar el código. Intenta de nuevo."

        composeRule.setContent {
            AndroidTheme(darkTheme = false) {
                SignInGenerateOtpScreen(
                    phone = "987654321",
                    onPhoneChange = {},
                    isValid = true,
                    loading = false,
                    state = OtpGenerateState.Error(errorMessage),
                    onSubmit = {}
                )
            }
        }

        composeRule
            .onNodeWithText(errorMessage)
            .assertExists()
    }
}
```

---

### Paso 4: Pruebas instrumentadas – integración con navegación (conceptual)

```kotlin
@RunWith(AndroidJUnit4::class)
class SignInGenerateOtpRouteTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `cuando el estado es Success se llama a onGoValidate`() {
        var navigatedPhone: String? = null
        var navigatedExpiresAt: String? = null

        composeRule.setContent {
            AndroidTheme(darkTheme = false) {
                SignInGenerateOtpRoute(
                    onGoValidate = { phone, expiresAt ->
                        navigatedPhone = phone
                        navigatedExpiresAt = expiresAt
                    }
                )
            }
        }

        // En una implementacion real: se forzaria el estado Success via Fake ViewModel.
        //
        // assertEquals("987654321", navigatedPhone)
        // assertEquals("2025-12-31T23:59:59Z", navigatedExpiresAt)
    }
}
```

---

## 5) Mocks y fakes aplicados a AuthRepository

### 5.1 Fake vs Mock

-   **FakeAuthRepository**  
    Implementación real mínima en memoria.  
    Útil para testear cambios de estado.

-   **MockAuthRepository**  
    Usado para verificar llamadas, parámetros y escenarios específicos.

#### Ejemplo de mock con MockK

```kotlin
@Test
fun `use case debe llamar al repo con prefijo 51`() = runTest {
    val repo = mockk<AuthRepository>()
    val expectedPhone = "51987654321"

    coEvery { repo.otpGenerate(expectedPhone) } returns OtpGenerateResult(
        expiresAt = "2025-12-31T23:59:59Z"
    )

    val useCase = OtpGenerateUseCase(repo)

    useCase("987654321").toList()

    coVerify(exactly = 1) { repo.otpGenerate(expectedPhone) }
}
```

---

## 6) Cierre de la sesión

En esta sesión hemos:

1. Justificado por qué el flujo de OTP es una pieza crítica que debe estar testeada.
2. Diferenciado:
    - Pruebas unitarias (lógica pura como `OtpGenerateUseCase`).
    - Pruebas de UI con Compose (estado de botones, mensajes de error, accesibilidad).
    - Pruebas instrumentadas (integración con Android, navegación, permisos).
3. Aplicado **fakes** y **mocks** sobre `AuthRepository` para desacoplar el código de la red real.
4. Visto ejemplos concretos de tests sobre tu pantalla `SignInGenerateOtpScreen`.
