package com.example.android_passenger.features.signup.presentation

import com.example.android_passenger.R
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.android_passenger.commons.presentation.GenericInputField
import com.example.android_passenger.commons.presentation.NavigationBarStyle
import com.example.android_passenger.commons.presentation.PrimaryButton
import com.example.android_passenger.core.presentation.utils.*
import com.example.android_passenger.features.signup.domain.model.SignUpModelStep1
import com.example.android_passenger.features.signup.domain.usecase.GetSignUpStep1UseCaseState
import com.example.android_passenger.features.signup.domain.usecase.UploadProfileImageUseCaseState
import kotlinx.coroutines.launch

@Composable
fun SignUpStep1Route(
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val step1State by viewModel.getStep1.collectAsState()
    val uploadImageState by viewModel.uploadImage.collectAsState()
    val currentPhotoUrl by viewModel.currentPhotoUrl.collectAsState()

    // Efecto para limpiar el estado de subida después de un tiempo
    LaunchedEffect(uploadImageState) {
        if (uploadImageState is UploadProfileImageUseCaseState.Error ||
            uploadImageState is UploadProfileImageUseCaseState.Success) {
            launch {
                kotlinx.coroutines.delay(3000)
                viewModel.clearImageUploadState()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.callGetStep1()
    }

    SignUpStep1Screen(
        step1State = step1State,
        uploadImageState = uploadImageState,
        currentPhotoUrl = currentPhotoUrl,
        onGivenNameChange = { /* Puedes manejar esto si es necesario */ },
        onFamilyNameChange = { /* Puedes manejar esto si es necesario */ },
        onImageSelected = { uri ->
            uri?.let { viewModel.uploadProfileImage(it) }
        },
        onNext = { givenName, familyName ->
            viewModel.callSaveStep1(
                SignUpModelStep1(
                    givenName = givenName,
                    familyName = familyName,
                    photoUrl = currentPhotoUrl ?: ""
                )
            )
            onNext()
        },
        modifier = modifier
    )
}

@Composable
fun SignUpStep1Screen(
    step1State: GetSignUpStep1UseCaseState,
    uploadImageState: UploadProfileImageUseCaseState,
    currentPhotoUrl: String?,
    onGivenNameChange: (String) -> Unit,
    onFamilyNameChange: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onNext: (givenName: String, familyName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = Color(0xFFF8F9FA)
    NavigationBarStyle(color = Color.White, darkIcons = true)

    var givenName by remember { mutableStateOf("") }
    var familyName by remember { mutableStateOf("") }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados de permisos
    val cameraPermissionState = rememberCameraPermissionState()
    val storagePermissionState = rememberStoragePermissionState()

    // Image Picker
    val imagePicker = rememberImagePicker { uri ->
        onImageSelected(uri)
    }

    // Launcher para crear archivo temporal para la cámara
    val tempFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/jpeg")
    ) { uri ->
        uri?.let {
            tempCameraUri = it
            imagePicker.openCamera(it)
        }
    }

    // Cargar datos existentes cuando el estado cambie
    LaunchedEffect(step1State) {
        when (step1State) {
            is GetSignUpStep1UseCaseState.Success -> {
                val data = step1State.data
                givenName = data.givenName ?: ""
                familyName = data.familyName ?: ""
            }
            else -> Unit
        }
    }

    val isStepLoading = step1State is GetSignUpStep1UseCaseState.Loading
    val isImageUploading = uploadImageState is UploadProfileImageUseCaseState.Loading
    val isLoading = isStepLoading || isImageUploading
    val isValid = givenName.isNotBlank() && familyName.isNotBlank() && !isLoading

    // Dialog para seleccionar fuente de imagen
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Seleccionar imagen") },
            text = { Text("¿De dónde quieres tomar la foto de perfil?") },
            confirmButton = {
                Button(
                    onClick = {
                        showImageSourceDialog = false
                        if (cameraPermissionState.hasPermission) {
                            // Crear archivo temporal para la cámara
                            tempFileLauncher.launch("profile_${System.currentTimeMillis()}.jpg")
                        } else {
                            cameraPermissionState.requestPermission()
                        }
                    },
                    enabled = !isImageUploading
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.feature_signup_ic_camera),
                        contentDescription = "Cámara",
                        modifier = Modifier.size(20.dp) // Control de tamaño
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Cámara")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showImageSourceDialog = false
                        if (storagePermissionState.hasPermission) {
                            imagePicker.openGallery()
                        } else {
                            storagePermissionState.requestPermission()
                        }
                    },
                    enabled = !isImageUploading
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.feature_signup_ic_image),
                        contentDescription = "Galería",
                        modifier = Modifier.size(20.dp) // Control de tamaño
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Galería")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .systemBarsPadding()
            .imePadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Información personal",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF1A1A1A)
            )

            Text(
                text = "Completa tus datos para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Image Picker Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar circular
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        currentPhotoUrl != null -> {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(currentPhotoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        isImageUploading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                strokeWidth = 3.dp
                            )
                        }
                        else -> {
                            Icon(
                                painter = painterResource(id = R.drawable.feature_signup_ic_camera),
                                contentDescription = "Agregar foto",
                                modifier = Modifier.size(32.dp), // Control de tamaño más pequeño
                                tint = Color.Gray
                            )
                        }
                    }
                }

                // Botón para cambiar imagen
                Button(
                    onClick = { showImageSourceDialog = true },
                    enabled = !isImageUploading
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.feature_signup_ic_image),
                        contentDescription = "Seleccionar foto",
                        modifier = Modifier.size(20.dp) // Control de tamaño
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (currentPhotoUrl != null) "Cambiar foto" else "Agregar foto"
                    )
                }

                // Mostrar estados de subida
                when (uploadImageState) {
                    is UploadProfileImageUseCaseState.Success -> {
                        Text(
                            text = "✓ Imagen subida correctamente",
                            color = Color.Green,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    is UploadProfileImageUseCaseState.Error -> {
                        Text(
                            text = uploadImageState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    else -> {}
                }
            }

            Spacer(Modifier.height(32.dp))

            // Campos de texto
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                GenericInputField(
                    value = givenName,
                    onValueChange = {
                        givenName = it
                        onGivenNameChange(it)
                    },
                    placeholder = "Ingresa tus nombres",
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                    maxLength = 50,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                    enabled = !isLoading
                )

                GenericInputField(
                    value = familyName,
                    onValueChange = {
                        familyName = it
                        onFamilyNameChange(it)
                    },
                    placeholder = "Ingresa tus apellidos",
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                    maxLength = 50,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                    enabled = !isLoading
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryButton(
                text = "Continuar",
                onClick = { onNext(givenName, familyName) },
                enabled = isValid,
                loading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/* ===== Previews ===== */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignUpStep1ScreenPreview_Empty() {
    SignUpStep1Screen(
        step1State = GetSignUpStep1UseCaseState.Success(
            SignUpModelStep1("", "", "")
        ),
        uploadImageState = UploadProfileImageUseCaseState.Idle,
        currentPhotoUrl = null,
        onGivenNameChange = {},
        onFamilyNameChange = {},
        onImageSelected = {},
        onNext = { _, _ -> }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignUpStep1ScreenPreview_Filled() {
    SignUpStep1Screen(
        step1State = GetSignUpStep1UseCaseState.Success(
            SignUpModelStep1("Juan", "Pérez", "https://example.com/photo.jpg")
        ),
        uploadImageState = UploadProfileImageUseCaseState.Idle,
        currentPhotoUrl = "https://example.com/photo.jpg",
        onGivenNameChange = {},
        onFamilyNameChange = {},
        onImageSelected = {},
        onNext = { _, _ -> }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignUpStep1ScreenPreview_Uploading() {
    SignUpStep1Screen(
        step1State = GetSignUpStep1UseCaseState.Success(
            SignUpModelStep1("Juan", "Pérez", "")
        ),
        uploadImageState = UploadProfileImageUseCaseState.Loading,
        currentPhotoUrl = null,
        onGivenNameChange = {},
        onFamilyNameChange = {},
        onImageSelected = {},
        onNext = { _, _ -> }
    )
}