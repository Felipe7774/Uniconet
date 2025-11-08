package com.utadeo.uniconnect.data.model.activity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.utadeo.uniconnect.data.model.navigation.AppScreens
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Define el color amarillo principal
private val YellowPrimary = Color(0xFFFDD835)
private val YellowBackground = Color(0xFFFFF9C4)
private val DarkText = Color(0xFF212121)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesListScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    var activities by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            activities = ActivitiesRepository.getAllActivities()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Actividades disponibles",
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = YellowPrimary
                )
            )
        },
        containerColor = YellowBackground
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
                        color = DarkText
                    )
                }
                activities.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💤",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay actividades registradas aún",
                            style = MaterialTheme.typography.bodyLarge,
                            color = DarkText.copy(alpha = 0.6f)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(activities) { activity ->
                            EnhancedActivityCard(
                                activity = activity,
                                onClick = {
                                    val id = activity["id"] as? String ?: return@EnhancedActivityCard
                                    navController.navigate(
                                        AppScreens.ActivityDetailScreen.createRoute(id)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedActivityCard(
    activity: Map<String, Any>,
    onClick: () -> Unit
) {
    val hasBudget = activity["hasBudget"] as? Boolean ?: false
    val budgetAmount = activity["budgetAmount"] as? Long ?: 0L

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header con título
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = (activity["title"] ?: "Sin título").toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Descripción
            Text(
                text = (activity["description"] ?: "Sin descripción").toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = DarkText.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Información del creador con timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoRow(
                    icon = Icons.Default.Person,
                    text = "Por ${activity["creatorName"]?.toString() ?: "Usuario"}",
                    backgroundColor = YellowPrimary.copy(alpha = 0.2f),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Tiempo transcurrido
                Text(
                    text = getTimeAgo(activity["timestamp"]),
                    style = MaterialTheme.typography.labelSmall,
                    color = DarkText.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fecha y hora
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow(
                    icon = Icons.Default.CalendarToday,
                    text = activity["date"]?.toString() ?: "Fecha no disponible",
                    modifier = Modifier.weight(1f)
                )

                InfoRow(
                    icon = Icons.Default.AccessTime,
                    text = activity["time"]?.toString() ?: "Hora no disponible",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ubicación
            InfoRow(
                icon = Icons.Default.LocationOn,
                text = activity["locationName"]?.toString() ?: "Sin ubicación",
                backgroundColor = Color(0xFFE3F2FD)
            )

            // Presupuesto (si existe)
            if (hasBudget && budgetAmount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(
                    icon = Icons.Default.AttachMoney,
                    text = "Presupuesto: $${budgetAmount}",
                    backgroundColor = Color(0xFFE8F5E9)
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFF5F5F5)
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DarkText.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = DarkText.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Función para calcular tiempo transcurrido
private fun getTimeAgo(timestamp: Any?): String {
    if (timestamp == null) return "Hace un momento"

    val time = when (timestamp) {
        is Long -> timestamp
        is com.google.firebase.Timestamp -> timestamp.toDate().time
        else -> return "Hace un momento"
    }

    val now = System.currentTimeMillis()
    val diff = now - time

    return when {
        diff < 60000 -> "Hace un momento"
        diff < 3600000 -> "Hace ${diff / 60000}m"
        diff < 86400000 -> "Hace ${diff / 3600000}h"
        diff < 604800000 -> "Hace ${diff / 86400000}d"
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale("es", "ES"))
            sdf.format(Date(time))
        }
    }
}