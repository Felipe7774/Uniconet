package com.utadeo.uniconnect.data.model.activity

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.utadeo.uniconnect.R
import kotlinx.coroutines.launch

private val YellowPrimary = Color(0xFFFDD835)

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    navController: NavController,
    activityId: String
) {
    val poppins = FontFamily(Font(R.font.poppins_regular))
    val scope = rememberCoroutineScope()

    var activityData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isParticipating by remember { mutableStateOf(false) }
    var isJoining by remember { mutableStateOf(false) }
    var participantsCount by remember { mutableStateOf(0) }

    LaunchedEffect(activityId) {
        scope.launch {
            isLoading = true
            activityData = ActivitiesRepository.getActivityById(activityId)
            isParticipating = ActivitiesRepository.isUserParticipating(activityId)
            participantsCount = activityData?.get("participantsCount") as? Int ?: 0
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detalles",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = YellowPrimary
                )
            )
        },
        containerColor = Color(0xFFFFF9C4)
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Black
                    )
                }
                activityData == null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("😕", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No se encontró la actividad",
                            fontFamily = poppins,
                            fontSize = 16.sp,
                            color = Color.Black.copy(alpha = 0.6f)
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Contenido scrolleable
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp)
                        ) {
                            ActivityDetailContent(
                                activity = activityData!!,
                                poppins = poppins,
                                participantsCount = participantsCount,
                                onParticipantsClick = {
                                    navController.navigate("participants/$activityId")
                                }
                            )
                        }

                        // Botón de unirse/salir (fijo en la parte inferior)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White,
                            shadowElevation = 8.dp
                        ) {
                            Button(
                                onClick = {
                                    if (!isJoining) {
                                        isJoining = true
                                        scope.launch {
                                            val success = if (isParticipating) {
                                                ActivitiesRepository.leaveActivity(activityId)
                                            } else {
                                                ActivitiesRepository.joinActivity(activityId)
                                            }

                                            if (success) {
                                                isParticipating = !isParticipating
                                                participantsCount += if (isParticipating) 1 else -1
                                            }
                                            isJoining = false
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isParticipating) Color.Red else Color.Black,
                                    disabledContainerColor = Color.Gray
                                ),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !isJoining
                            ) {
                                if (isJoining) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isParticipating)
                                            Icons.Default.ExitToApp
                                        else
                                            Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isParticipating)
                                            "Salir de la actividad"
                                        else
                                            "Unirme a la actividad",
                                        fontFamily = poppins,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityDetailContent(
    activity: Map<String, Any>,
    poppins: FontFamily,
    participantsCount: Int,
    onParticipantsClick: () -> Unit
) {
    val lat = activity["locationLat"] as? Double
    val lng = activity["locationLng"] as? Double
    val hasMap = lat != null && lng != null

    // Header con creador
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(YellowPrimary.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = activity["creatorName"]?.toString() ?: "Usuario",
                fontFamily = poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Text(
                text = "Creador de la actividad",
                fontFamily = poppins,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Título
    Text(
        text = activity["title"]?.toString() ?: "Sin título",
        fontFamily = poppins,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        lineHeight = 32.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Descripción
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = activity["description"]?.toString() ?: "Sin descripción",
            fontFamily = poppins,
            fontSize = 15.sp,
            color = Color.Black,
            lineHeight = 24.sp,
            modifier = Modifier.padding(16.dp)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Información
    InfoSection(
        title = "Información",
        poppins = poppins
    ) {
        InfoRow(
            icon = Icons.Default.CalendarToday,
            label = "Fecha",
            value = activity["date"]?.toString() ?: "No especificada",
            poppins = poppins
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoRow(
            icon = Icons.Default.AccessTime,
            label = "Hora",
            value = activity["time"]?.toString() ?: "No especificada",
            poppins = poppins
        )

        if (activity["hasBudget"] == true) {
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(
                icon = Icons.Default.AttachMoney,
                label = "Presupuesto",
                value = "$${activity["budgetAmount"]} COP",
                poppins = poppins
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Participantes
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onParticipantsClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Participantes",
                        fontFamily = poppins,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "$participantsCount persona${if (participantsCount != 1) "s" else ""}",
                        fontFamily = poppins,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }

    // Mapa
    if (hasMap) {
        Spacer(modifier = Modifier.height(16.dp))

        InfoSection(
            title = "Ubicación",
            poppins = poppins
        ) {
            Text(
                text = activity["locationName"]?.toString() ?: "Sin nombre",
                fontFamily = poppins,
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            MapPreview(LatLng(lat!!, lng!!))
        }
    }

    Spacer(modifier = Modifier.height(100.dp))
}

@Composable
fun InfoSection(
    title: String,
    poppins: FontFamily,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontFamily = poppins,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    poppins: FontFamily
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontFamily = poppins,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontFamily = poppins,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun MapPreview(location: LatLng) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 15f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = location),
                title = "Ubicación"
            )
        }
    }
}