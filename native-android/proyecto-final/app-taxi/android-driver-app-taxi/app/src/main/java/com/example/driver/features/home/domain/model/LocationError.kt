package com.example.driver.features.home.domain.model

enum class LocationError { SettingsDisabled, PermissionMissing, Unavailable }

class LocationException(val error: LocationError) : Exception(error.name)
