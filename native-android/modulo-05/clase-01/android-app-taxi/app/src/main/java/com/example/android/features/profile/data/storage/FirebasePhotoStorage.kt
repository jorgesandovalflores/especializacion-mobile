package com.example.android.features.profile.data.storage

import android.content.Context
import com.example.android.core.data.safeCall
import com.example.android.core.domain.DomainException
import com.example.android.features.profile.data.remote.ProfileApi
import com.example.android.features.profile.domain.model.UploadEvent
import com.example.android.features.profile.domain.repository.PhotoStorage
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebasePhotoStorage(
    private val context: Context,
    private val api: ProfileApi
) : PhotoStorage {

    override fun upload(image: ByteArray): Flow<UploadEvent> = flow {
        if (FirebaseApp.getApps(context).isEmpty()) throw notConfigured()

        val access = safeCall { api.storageToken() }
        FirebaseAuth.getInstance().signInWithCustomToken(access.token).await()

        val ref = FirebaseStorage.getInstance("gs://${access.bucket}").reference.child(access.photoPath)
        emitAll(ref.putBytesWithProgress(image))
    }.catch { throw it.toUploadException() }

    private fun com.google.firebase.storage.StorageReference.putBytesWithProgress(
        image: ByteArray
    ): Flow<UploadEvent> = callbackFlow {
        val metadata = StorageMetadata.Builder().setContentType(JPEG).build()
        val task = putBytes(image, metadata)
            .addOnProgressListener { snapshot ->
                val total = snapshot.totalByteCount.coerceAtLeast(1)
                trySend(UploadEvent.Progress((snapshot.bytesTransferred * 100 / total).toInt()))
            }
            .addOnSuccessListener {
                launch {
                    runCatching { downloadUrl.await() }
                        .onSuccess { send(UploadEvent.Done(it.toString())); close() }
                        .onFailure { close(it) }
                }
            }
            .addOnFailureListener { close(it) }

        awaitClose { if (!task.isComplete) task.cancel() }
    }

    private fun Throwable.toUploadException(): Throwable = when (this) {
        is DomainException -> this
        is FirebaseNetworkException -> DomainException.NetworkException("No se pudo subir la foto. Revisa tu conexión", causeThrowable = this)
        is StorageException -> when (errorCode) {
            StorageException.ERROR_NOT_AUTHENTICATED,
            StorageException.ERROR_NOT_AUTHORIZED -> DomainException.ClientException("No tienes permiso para cambiar esta foto", causeThrowable = this)
            StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> DomainException.NetworkException("No se pudo subir la foto. Revisa tu conexión", causeThrowable = this)
            else -> DomainException.ServerException("No se pudo subir la foto. Inténtalo más tarde", causeThrowable = this)
        }
        else -> this
    }

    private fun notConfigured() =
        DomainException.ClientException("La subida de fotos no está disponible en esta versión")

    private companion object {
        const val JPEG = "image/jpeg"
    }
}
