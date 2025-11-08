package com.utadeo.uniconnect.data.model.ui.Login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.ui.theme.black
import com.utadeo.uniconnect.ui.theme.white
import com.utadeo.uniconnect.ui.theme.primary_color
import com.utadeo.uniconnect.data.model.navigation.AppScreens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore
    val uid = auth.currentUser?.uid

    // Estados de configuración
    var notificationsEnabled by remember { mutableStateOf(true) }
    var locationEnabled by remember { mutableStateOf(false) }
    var privateProfile by remember { mutableStateOf(false) }
    var onlineStatus by remember { mutableStateOf(true) }

    var loading by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // Cargar datos del usuario
    LaunchedEffect(uid) {
        if (uid == null) {
            loadError = "Usuario no autenticado"
            return@LaunchedEffect
        }
        loading = true
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: notificationsEnabled
                    locationEnabled = doc.getBoolean("locationEnabled") ?: locationEnabled
                    privateProfile = doc.getBoolean("privateProfile") ?: privateProfile
                    onlineStatus = doc.getBoolean("onlineStatus") ?: onlineStatus
                }
                loading = false
            }
            .addOnFailureListener { e ->
                loadError = e.message
                loading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configuración",
                        color = black,
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primary_color)
            )
        },
        containerColor = primary_color
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .background(white)
        ) {
            Text(
                text = "Preferencias de Usuario",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = black,
                modifier = Modifier.padding(vertical = 8.dp),
                fontFamily = FontFamily(Font(R.font.poppins_regular))
            )

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
            }

            SettingItem(
                icon = Icons.Default.Public,
                label = "Idioma"
            ) {
                Text(
                    text = "Español",
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    color = Color.Gray
                )
            }

            SettingSwitchItem(
                icon = Icons.Default.Notifications,
                label = "Notificaciones",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )

            SettingSwitchItem(
                icon = Icons.Default.LocationOn,
                label = "Ubicación",
                checked = locationEnabled,
                onCheckedChange = { locationEnabled = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Privacidad",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = black,
                modifier = Modifier.padding(vertical = 8.dp),
                fontFamily = FontFamily(Font(R.font.poppins_regular))
            )

            SettingSwitchItem(
                icon = Icons.Default.Person,
                label = "Perfil Privado",
                checked = privateProfile,
                onCheckedChange = { privateProfile = it }
            )

            SettingSwitchItem(
                icon = Icons.Default.Public,
                label = "Estado en Línea",
                checked = onlineStatus,
                onCheckedChange = { onlineStatus = it }
            )

            Spacer(modifier = Modifier.weight(1f))

            loadError?.let {
                Text(text = "Error al cargar: $it", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Botón Guardar Cambios
            Button(
                onClick = {
                    if (uid == null) {
                        Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    saving = true
                    val updates = mapOf(
                        "notificationsEnabled" to notificationsEnabled,
                        "locationEnabled" to locationEnabled,
                        "privateProfile" to privateProfile,
                        "onlineStatus" to onlineStatus
                    )
                    db.collection("users").document(uid)
                        .set(updates, SetOptions.merge())
                        .addOnSuccessListener {
                            saving = false
                            Toast.makeText(context, "Cambios guardados", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            saving = false
                            Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !saving
            ) {
                if (saving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GUARDANDO", color = white)
                } else {
                    Text(
                        text = "GUARDAR CAMBIOS",
                        color = white,
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔥 Botón CERRAR SESIÓN
            Button(
                onClick = {
                    auth.signOut()
                    Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    navController.navigate(AppScreens.SplashScreen.route) {
                        popUpTo(0) { inclusive = true } // limpia la pila de navegación
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "CERRAR SESIÓN",
                    color = white,
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    label: String,
    trailingContent: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = black)
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, fontFamily = FontFamily(Font(R.font.poppins_regular)), color = black)
        }
        trailingContent()
    }
}

@Composable
fun SettingSwitchItem(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingItem(icon, label) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                uncheckedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4CAF50),
                uncheckedTrackColor = Color(0xFFBDBDBD),
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun settingsPreview() {
    val navController = rememberNavController()
    SettingsScreen(navController = navController)
}
