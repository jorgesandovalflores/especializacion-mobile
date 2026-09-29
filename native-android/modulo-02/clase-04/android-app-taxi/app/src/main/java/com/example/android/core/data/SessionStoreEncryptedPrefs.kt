package com.example.android.core.data

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.core.content.edit
import com.example.android.core.domain.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SessionStoreEncryptedPrefs(
    context: Context,
    prefsName: String = DEFAULT_PREFS_NAME
) : SessionStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    private val secretKey: SecretKey by lazy { loadOrCreateKey() }

    override suspend fun saveTokens(access: String, refresh: String) {
        withContext(Dispatchers.IO) {
            prefs.edit {
                putString(KEY_ACCESS, encrypt(KEY_ACCESS, access))
                putString(KEY_REFRESH, encrypt(KEY_REFRESH, refresh))
            }
        }
    }

    override fun accessToken(): Flow<String?> = prefs.asFlow(KEY_ACCESS)
    override fun refreshToken(): Flow<String?> = prefs.asFlow(KEY_REFRESH)

    override suspend fun clear() { prefs.edit { clear() } }

    private fun SharedPreferences.asFlow(key: String): Flow<String?> = callbackFlow {
        trySend(decrypt(key, getString(key, null)))
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, k ->
            if (k == key) trySend(decrypt(key, sp.getString(k, null)))
        }
        registerOnSharedPreferenceChangeListener(listener)
        awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun currentAccessToken(): String? = runBlocking { accessToken().first() }

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
        const val DEFAULT_PREFS_NAME = "session_store"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "session_store_aes_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val AES_KEY_SIZE_BITS = 256
        private const val GCM_IV_SIZE_BYTES = 12
        private const val GCM_TAG_SIZE_BITS = 128
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
    }
}
