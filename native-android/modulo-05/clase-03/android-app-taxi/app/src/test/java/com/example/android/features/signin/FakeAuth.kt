package com.example.android.features.signin

import com.example.android.commons.domain.model.Passenger
import com.example.android.core.domain.SessionStore
import com.example.android.features.signin.domain.model.OtpGenerateResult
import com.example.android.features.signin.domain.model.OtpValidateResult
import com.example.android.features.signin.domain.model.SessionTokens
import com.example.android.features.signin.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository(
    private val onGenerate: suspend (String) -> OtpGenerateResult = { OtpGenerateResult("2026-09-28T15:02:00Z") },
    private val onValidate: suspend (String, String) -> OtpValidateResult = { phone, _ -> validResult(phone, "ACTIVE") }
) : AuthRepository {
    val generatedFor = mutableListOf<String>()
    val validatedWith = mutableListOf<Pair<String, String>>()

    override suspend fun otpGenerate(phone: String): OtpGenerateResult {
        generatedFor += phone
        return onGenerate(phone)
    }

    override suspend fun otpValidate(phone: String, code: String): OtpValidateResult {
        validatedWith += phone to code
        return onValidate(phone, code)
    }
}

class FakeSessionStore : SessionStore {
    private val access = MutableStateFlow<String?>(null)
    private val refresh = MutableStateFlow<String?>(null)
    private val pending = MutableStateFlow(false)

    override suspend fun saveTokens(access: String, refresh: String) {
        this.access.value = access
        this.refresh.value = refresh
    }

    override fun accessToken(): Flow<String?> = access
    override fun refreshToken(): Flow<String?> = refresh

    override suspend fun setRegistrationPending(pending: Boolean) {
        this.pending.value = pending
    }

    override fun registrationPending(): Flow<Boolean> = pending

    override suspend fun clear() {
        access.value = null
        refresh.value = null
        pending.value = false
    }

    fun currentAccess(): String? = access.value
    fun currentRefresh(): String? = refresh.value
    fun isRegistrationPending(): Boolean = pending.value
}

fun validResult(phone: String, status: String) = OtpValidateResult(
    tokens = SessionTokens(accessToken = "access", refreshToken = "refresh"),
    user = Passenger(
        id = "1",
        phoneNumber = phone,
        givenName = "Jorge",
        familyName = null,
        email = null,
        photoUrl = null,
        status = status
    )
)
