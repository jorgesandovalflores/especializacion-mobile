package com.example.android.features.signup.data.repository

import com.example.android.commons.domain.usecase.AuthResult
import com.example.android.core.domain.ErrorMapper
import com.example.android.features.signup.data.remote.SignupApi
import com.example.android.features.signup.data.remote.dto.SignUpRequest
import com.example.android.features.signup.data.remote.dto.toDomain
import com.example.android.features.signup.domain.repository.SignUpRepository

class SignUpRepositoryImpl(
    private val api: SignupApi
): SignUpRepository {

    override suspend fun signUpRemote(givenName: String, familyName: String, photoUrl: String, email: String): AuthResult {
        return runCatching {
            api.signUp(
                SignUpRequest(
                    givenName = givenName,
                    familyName = familyName,
                    photoUrl = photoUrl,
                    email = email
                )
            ).toDomain()
        }.getOrElse { throw ErrorMapper.map(it) }
    }

}