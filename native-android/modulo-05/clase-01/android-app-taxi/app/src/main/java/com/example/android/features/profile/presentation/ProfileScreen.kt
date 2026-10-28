package com.example.android.features.profile.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.android.commons.domain.model.Passenger
import com.example.android.commons.presentation.MOTION_MEDIUM_MS
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.commons.presentation.PrimaryButton
import com.example.android.commons.presentation.ToastHost
import com.example.android.commons.presentation.ToastMessage
import com.example.android.commons.presentation.ToastType
import com.example.android.commons.presentation.formatPeruPhone
import com.example.android.core.presentation.theme.ColorPrimary
import com.example.android.features.profile.domain.usecase.PhotoState
import com.example.android.features.profile.domain.usecase.ProfileState
import kotlinx.coroutines.delay

private const val TOAST_MS = 3_000L
private const val PHOTO_SUCCESS = "Foto actualizada"
private val ColorText = Color(0xFF0F0F0F)
private val ColorTextSecondary = Color(0xFF6B6B6B)

@Composable
fun ProfileRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: ProfileViewModel = hiltViewModel()
) {
    val profile by vm.profile.collectAsStateWithLifecycle()
    val photo by vm.photo.collectAsStateWithLifecycle()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { vm.onPhotoPicked(it.toString()) }
    }

    LaunchedEffect(photo) {
        if (photo is PhotoState.Success || photo is PhotoState.Error) {
            delay(TOAST_MS)
            vm.onToastShown()
        }
    }

    ProfileScreen(
        profile = profile,
        photo = photo,
        onBack = onBack,
        onChangePhoto = {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onRetry = vm::load,
        modifier = modifier
    )
}

@Composable
fun ProfileScreen(
    profile: ProfileState,
    photo: PhotoState?,
    onBack: () -> Unit,
    onChangePhoto: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBarStyle(darkIcons = true)

    val uploading = photo is PhotoState.Preparing || photo is PhotoState.Uploading
    val toast = when (photo) {
        is PhotoState.Success -> ToastMessage(ToastType.Success, PHOTO_SUCCESS)
        is PhotoState.Error -> ToastMessage(ToastType.Error, photo.message)
        else -> null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            ProfileTopBar(onBack = onBack)

            AnimatedContent(
                targetState = profile,
                contentKey = { it::class },
                transitionSpec = { fadeIn(tween(MOTION_MEDIUM_MS)) togetherWith fadeOut(tween(MOTION_MEDIUM_MS)) },
                label = "profileContent"
            ) { current ->
                when (current) {
                    ProfileState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ColorPrimary)
                    }
                    is ProfileState.Error -> ProfileError(message = current.message, onRetry = onRetry)
                    is ProfileState.Success -> ProfileContent(
                        passenger = current.passenger,
                        photo = photo,
                        uploading = uploading,
                        onChangePhoto = onChangePhoto
                    )
                }
            }
        }

        ToastHost(toast = toast, modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun ProfileTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            onClick = onBack,
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = ColorText
                )
            }
        }
        Text(
            text = "Mi perfil",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = ColorText
        )
    }
}

@Composable
private fun ProfileContent(
    passenger: Passenger,
    photo: PhotoState?,
    uploading: Boolean,
    onChangePhoto: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Avatar(passenger = passenger, photo = photo, uploading = uploading)

        Spacer(Modifier.height(16.dp))
        Text(
            text = passenger.fullName(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = ColorText
        )
        Text(
            text = formatPeruPhone(passenger.phoneNumber.removePrefix("51")),
            fontSize = 15.sp,
            color = ColorTextSecondary
        )

        Spacer(Modifier.height(24.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ProfileRow(label = "Correo", value = passenger.email ?: "Sin correo")
                ProfileRow(label = "Estado", value = if (passenger.status == "ACTIVE") "Activo" else passenger.status)
            }
        }

        Spacer(Modifier.weight(1f))
        PrimaryButton(
            text = if (passenger.photoUrl == null) "Agregar foto" else "Cambiar foto",
            onClick = onChangePhoto,
            loading = uploading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Avatar(passenger: Passenger, photo: PhotoState?, uploading: Boolean) {
    val transition = updateTransition(targetState = uploading, label = "avatarUpload")
    val avatarScale by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy) },
        label = "avatarScale"
    ) { if (it) 0.9f else 1f }
    val overlayAlpha by transition.animateFloat(
        transitionSpec = { tween(MOTION_MEDIUM_MS) },
        label = "overlayAlpha"
    ) { if (it) 1f else 0f }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(136.dp)) {
        val avatarModifier = Modifier
            .size(128.dp)
            .graphicsLayer {
                scaleX = avatarScale
                scaleY = avatarScale
            }
            .clip(CircleShape)
            .background(ColorPrimary.copy(alpha = 0.12f))
        val url = passenger.photoUrl
        if (url != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(url)
                    .crossfade(MOTION_MEDIUM_MS)
                    .build(),
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = avatarModifier
            )
        } else {
            Box(contentAlignment = Alignment.Center, modifier = avatarModifier) {
                Text(text = passenger.initials(), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = ColorPrimary)
            }
        }

        if (transition.currentState || transition.targetState) {
            UploadOverlay(photo = photo, modifier = Modifier.graphicsLayer { alpha = overlayAlpha })
        }
    }
}

@Composable
private fun UploadOverlay(photo: PhotoState?, modifier: Modifier = Modifier) {
    val percent = when (photo) {
        is PhotoState.Uploading -> photo.percent
        is PhotoState.Success -> 100
        else -> null
    }
    val progress by animateFloatAsState(
        targetValue = (percent ?: 0) / 100f,
        animationSpec = tween(MOTION_MEDIUM_MS),
        label = "uploadProgress"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(136.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
        ) {
            Text(
                text = percent?.let { "$it %" } ?: "…",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (percent == null) {
            CircularProgressIndicator(color = ColorPrimary, strokeWidth = 4.dp, modifier = Modifier.fillMaxSize())
        } else {
            CircularProgressIndicator(
                progress = { progress },
                color = ColorPrimary,
                trackColor = ColorPrimary.copy(alpha = 0.15f),
                strokeWidth = 4.dp,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = ColorTextSecondary, fontSize = 14.sp)
        Text(text = value, color = ColorText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ProfileError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = ColorText, fontSize = 15.sp)
        Spacer(Modifier.height(16.dp))
        PrimaryButton(text = "Reintentar", onClick = onRetry, modifier = Modifier.fillMaxWidth())
    }
}

private fun Passenger.fullName(): String =
    listOfNotNull(givenName, familyName).joinToString(" ").ifBlank { "Pasajero" }

private fun Passenger.initials(): String =
    listOfNotNull(givenName, familyName)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "?" }

private val previewPassenger = Passenger(
    id = "1",
    phoneNumber = "51987654321",
    givenName = "Jorge",
    familyName = "Sandoval",
    email = "jorge@example.com",
    photoUrl = null,
    status = "ACTIVE"
)

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun ProfileScreenPreviewNoPhoto() {
    ProfileScreen(ProfileState.Success(previewPassenger), null, {}, {}, {})
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun ProfileScreenPreviewPreparing() {
    ProfileScreen(ProfileState.Success(previewPassenger), PhotoState.Preparing, {}, {}, {})
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun ProfileScreenPreviewUploading() {
    ProfileScreen(ProfileState.Success(previewPassenger), PhotoState.Uploading(45), {}, {}, {})
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun ProfileScreenPreviewError() {
    ProfileScreen(
        ProfileState.Success(previewPassenger),
        PhotoState.Error("No se pudo subir la foto. Revisa tu conexión"),
        {}, {}, {}
    )
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun ProfileScreenPreviewLoading() {
    ProfileScreen(ProfileState.Loading, null, {}, {}, {})
}
