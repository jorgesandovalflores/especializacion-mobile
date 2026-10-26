package com.example.android.features.profile

import com.example.android.commons.domain.model.Passenger
import com.example.android.core.domain.DomainException
import com.example.android.features.profile.domain.model.UploadEvent
import com.example.android.features.profile.domain.repository.ImageCompressor
import com.example.android.features.profile.domain.repository.PhotoStorage
import com.example.android.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun passenger(photoUrl: String? = null) = Passenger(
    id = "passenger-1",
    phoneNumber = "51987654321",
    givenName = "Ana",
    familyName = "Perez",
    email = "ana@example.com",
    photoUrl = photoUrl,
    status = "ACTIVE"
)

class FakeProfileRepository : ProfileRepository {
    var profile = passenger()
    var failure: DomainException? = null
    val savedUrls = mutableListOf<String>()

    override suspend fun me(): Passenger {
        failure?.let { throw it }
        return profile
    }

    override suspend fun savePhoto(url: String): Passenger {
        failure?.let { throw it }
        savedUrls += url
        profile = profile.copy(photoUrl = url)
        return profile
    }
}

class FakePhotoStorage : PhotoStorage {
    var events: List<UploadEvent> = listOf(
        UploadEvent.Progress(0),
        UploadEvent.Progress(50),
        UploadEvent.Progress(100),
        UploadEvent.Done("https://storage/avatar.jpg")
    )
    var failure: DomainException? = null
    val uploads = mutableListOf<ByteArray>()

    override fun upload(image: ByteArray): Flow<UploadEvent> = flow {
        uploads += image
        failure?.let { throw it }
        events.forEach { emit(it) }
    }
}

class FakeImageCompressor : ImageCompressor {
    val compressed = mutableListOf<String>()
    override suspend fun compress(uri: String): ByteArray {
        compressed += uri
        return byteArrayOf(1, 2, 3)
    }
}
