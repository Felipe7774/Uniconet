package com.utadeo.uniconnect.data.model.ui.Register

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.data.model.navigation.AppScreens
import com.utadeo.uniconnect.data.manager.RegistrationManager
import coil.compose.AsyncImage

@Composable
fun ProfilePictureScreen(navController: NavController) {
    val context = LocalContext.current
    val registrationManager = remember { RegistrationManager(context) }
    val poppins = FontFamily(Font(R.font.poppins_regular))

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var profilePictureSelected by remember { mutableStateOf(false) }

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedImageUri = uri
            profilePictureSelected = uri != null
        }
    )

    val takePicturePreview = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            if (bitmap != null) {
                selectedBitmap = bitmap
                profilePictureSelected = true
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.primary_color))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 40.dp)
        ) {
            // Botón volver
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = colorResource(R.color.black),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Foto de perfil",
                fontSize = 40.sp,
                fontFamily = poppins,
                color = colorResource(R.color.black),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(60.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(
                            if (profilePictureSelected) colorResource(R.color.black)
                            else colorResource(R.color.white)
                        )
                        .border(
                            width = 3.dp,
                            color = colorResource(R.color.black),
                            shape = CircleShape
                        )
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Seleccionada",
                            modifier = Modifier
                                .size(200.dp)
                                .clip(CircleShape)
                        )
                    } else if (selectedBitmap != null) {
                        AsyncImage(
                            model = selectedBitmap,
                            contentDescription = "Seleccionada",
                            modifier = Modifier
                                .size(200.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar foto",
                            tint = colorResource(R.color.black),
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Elige una foto desde tu galeria o toma una instantanea con tu cámara",
                    fontSize = 30.sp,
                    color = colorResource(R.color.black),
                    fontFamily = poppins,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "(Opcional)",
                    fontSize = 30.sp,
                    color = colorResource(R.color.black),
                    fontFamily = poppins,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botones galería / cámara
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = {
                        pickMedia.launch(PickVisualMediaRequest(ImageOnly))
                    }) {
                        Text("Galería")
                    }
                    OutlinedButton(onClick = {
                        takePicturePreview.launch(null)
                    }) {
                        Text("Cámara")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (processing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
            }

            errorMessage?.let { err ->
                Text("Error: $err", color = Color.Red, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Botones Omitir / Continuar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        // ✅ MODIFICADO: Ir a Bio sin guardar foto
                        navController.navigate(AppScreens.UserBioScreen.route)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(55.dp),
                    shape = RoundedCornerShape(25.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(R.color.black)
                    )
                ) {
                    Text("Omitir", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = poppins)
                }

                Button(
                    onClick = {
                        processing = true
                        errorMessage = null

                        try {
                            // ✅ MODIFICADO: Guardar imagen localmente, NO subir
                            if (selectedImageUri != null) {
                                registrationManager.saveProfileImage(selectedImageUri!!)
                            } else if (selectedBitmap != null) {
                                registrationManager.saveProfileImageFromBitmap(selectedBitmap!!)
                            }

                            processing = false
                            // ✅ MODIFICADO: Ir a Bio, NO a Home
                            navController.navigate(AppScreens.UserBioScreen.route)

                        } catch (e: Exception) {
                            processing = false
                            errorMessage = e.message
                            Log.e("ProfilePicture", "Error al guardar: ${e.message}")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(55.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.black),
                        contentColor = colorResource(R.color.white)
                    ),
                    shape = RoundedCornerShape(25.dp),
                    enabled = !processing
                ) {
                    Text("Continuar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = poppins)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}