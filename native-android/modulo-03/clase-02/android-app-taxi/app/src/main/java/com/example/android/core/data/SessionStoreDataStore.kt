package com.example.android.core.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.android.core.domain.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = SessionStoreDataStore.DEFAULT_STORE_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, SessionStoreEncryptedPrefs.DEFAULT_PREFS_NAME))
    }
)

class SessionStoreDataStore(
    private val dataStore: DataStore<Preferences>
) : SessionStore {

    constructor(context: Context) : this(context.sessionDataStore)

    private val secretKey: SecretKey by lazy { loadOrCreateKey() }

    private val preferences: Flow<Preferences> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }

    override suspend fun saveTokens(access: String, refresh: String) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS] = encrypt(KEY_ACCESS.name, access)
            prefs[KEY_REFRESH] = encrypt(KEY_REFRESH.name, refresh)
        }
    }

    override fun accessToken(): Flow<String?> = tokenFlow(KEY_ACCESS)
    override fun refreshToken(): Flow<String?> = tokenFlow(KEY_REFRESH)

    override suspend fun setRegistrationPending(pending: Boolean) {
        dataStore.edit { it[KEY_REGISTRATION_PENDING] = pending }
    }

    override fun registrationPending(): Flow<Boolean> = preferences
        .map { it[KEY_REGISTRATION_PENDING] ?: false }
        .distinctUntilChanged()

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private fun tokenFlow(key: Preferences.Key<String>): Flow<String?> = preferences
        .map { decrypt(key.name, it[key]) }
        .distinctUntilChanged()

    private fun loadOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(AES_KEY_SIZE_BITS)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            .apply { init(spec) }
            .generateKey()
    }

    private fun encrypt(key: String, plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        cipher.updateAAD(key.toByteArray(Charsets.UTF_8))
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(cipher.iv + cipherText)
    }

    private fun decrypt(key: String, encoded: String?): String? {
        if (encoded == null) return null
        return try {
            val payload = Base64.getDecoder().decode(encoded)
            val iv = payload.copyOfRange(0, GCM_IV_SIZE_BYTES)
            val cipherText = payload.copyOfRange(GCM_IV_SIZE_BYTES, payload.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_SIZE_BITS, iv))
            cipher.updateAAD(key.toByteArray(Charsets.UTF_8))
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        const val DEFAULT_STORE_NAME = "session_store"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "session_store_aes_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val AES_KEY_SIZE_BITS = 256
        private const val GCM_IV_SIZE_BYTES = 12
        private const val GCM_TAG_SIZE_BITS = 128
        private val KEY_ACCESS = stringPreferencesKey("access_token")
        private val KEY_REFRESH = stringPreferencesKey("refresh_token")
        private val KEY_REGISTRATION_PENDING = booleanPreferencesKey("registration_pending")
    }
}
