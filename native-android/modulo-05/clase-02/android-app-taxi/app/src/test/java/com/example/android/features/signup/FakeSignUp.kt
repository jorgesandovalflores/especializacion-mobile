package com.example.android.features.signup

import com.example.android.commons.domain.model.Passenger
import com.example.android.features.signup.domain.model.SignUpDraft
import com.example.android.features.signup.domain.repository.SignUpRepository
import com.example.android.features.signup.domain.store.SignUpDraftStore

class FakeSignUpDraftStore(initial: SignUpDraft = SignUpDraft()) : SignUpDraftStore {
    var current = initial
        private set

    override suspend fun get(): SignUpDraft = current

    override suspend fun savePersonal(givenName: String, familyName: String) {
        current = current.copy(givenName = givenName, familyName = familyName)
    }

    override suspend fun saveEmail(email: String) {
        current = current.copy(email = email)
    }

    override suspend fun clear() {
        current = SignUpDraft()
    }
}

class FakeSignUpRepository(
    private val onSignUp: suspend (String, String, String) -> Passenger = { givenName, familyName, email ->
        Passenger(
            id = "1",
            phoneNumber = "51987654321",
            givenName = givenName,
            familyName = familyName,
            email = email,
            photoUrl = null,
            status = "ACTIVE"
        )
    }
) : SignUpRepository {
    val requests = mutableListOf<Triple<String, String, String>>()

    override suspend fun signUp(givenName: String, familyName: String, email: String): Passenger {
        requests += Triple(givenName, familyName, email)
        return onSignUp(givenName, familyName, email)
    }
}
