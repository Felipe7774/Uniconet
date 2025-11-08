package com.utadeo.uniconnect.data.model.activity

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import java.util.Locale
import android.location.Geocoder

@SuppressLint("MissingPermission")
@Composable
fun MapPickerScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedPosition by remember { mutableStateOf<LatLng?>(null) }
    var selectedAddress by remember { mutableStateOf<String?>(null) }

    // Posición inicial del mapa (Bogotá por defecto)
    val initialPosition = LatLng(4.7110, -74.0721)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 12f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                selectedPosition = latLng

                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addressList = geocoder.getFromLocation(
                        latLng.latitude,
                        latLng.longitude,
                        1
                    )
                    selectedAddress = addressList?.firstOrNull()?.getAddressLine(0)
                } catch (e: Exception) {
                    selectedAddress = "Ubicación seleccionada"
                }
            }
        ) {
            selectedPosition?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Ubicación seleccionada",
                    snippet = selectedAddress
                )
            }
        }

        selectedAddress?.let {
            Text(
                text = "📍 $it",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // ✅ Botón de confirmación - ARREGLADO
        Button(
            onClick = {
                selectedPosition?.let { pos ->
                    // 🔥 Guardamos los datos en el savedStateHandle
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("selected_address", selectedAddress ?: "Ubicación seleccionada")
                        set("selected_latlng", pos)
                    }
                    // Volvemos a la pantalla anterior (ActivityCreateScreen)
                    navController.popBackStack()
                }
            },
            enabled = selectedPosition != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Text("Confirmar ubicación")
        }
    }
}