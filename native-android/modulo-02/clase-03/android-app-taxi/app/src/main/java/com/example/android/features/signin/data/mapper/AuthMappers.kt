package com.example.android.features.signin.data.mapper

import com.example.android.features.signin.data.remote.dto.PassengerDto
import com.example.android.features.signin.data.remote.dto.PassengerLoginResponse
import com.example.android.features.signin.domain.model.Passenger
import com.example.android.features.signin.domain.model.SessionTokens
import com.example.android.features.signin.domain.repository.SignInResult

fun PassengerDto.toDomain(): Passenger = Passenger(
    id = id,
    phoneNumber = phoneNumber,
    givenName = givenName,
    familyName = familyName,
    email = email,
    photoUrl = photoUrl,
    status = status
)

fun PassengerLoginResponse.toDomain(): SignInResult = SignInResult(
    tokens = SessionTokens(
        accessToken = accessToken,
        refreshToken = refreshToken
    ),
    user = user.toDomain()
)