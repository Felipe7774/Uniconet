package com.utadeo.uniconnect.data.model.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.data.model.navigation.AppScreens

@Composable
fun ActivityFlowScreen(navController: NavController) {
    val poppins = FontFamily(Font(R.font.poppins_regular))

    var selectedCard by remember { mutableStateOf<String?>(null) }
    var preguntaOption by remember { mutableStateOf<String?>(null) }
    var actividadOption by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDD835))
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título principal
            Text(
                text = "¿Que te gustaría\nhacer?",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = poppins,
                color = Color.Black,
                textAlign = TextAlign.Center,
                lineHeight = 42.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Elige una opción\npara comenzar",
                fontSize = 20.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(70.dp))

            // Tarjeta PREGUNTA
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(190.dp)
                    .clickable {
                        selectedCard = "pregunta"
                        actividadOption = null
                    }
                    .alpha(if (selectedCard == null || selectedCard == "pregunta") 1f else 0.4f),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedCard == "pregunta")
                        Color.Black
                    else
                        Color.Black.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Título en la parte superior (centrado)
                    Text(
                        text = "Pregunta",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = poppins,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    // Opciones en la parte inferior
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Opción: Crear una pregunta
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable(enabled = selectedCard == "pregunta") {
                                    preguntaOption = "crear"
                                }
                        ) {
                            RadioButton(
                                selected = preguntaOption == "crear",
                                onClick = { preguntaOption = "crear" },
                                enabled = selectedCard == "pregunta",
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color.White,
                                    unselectedColor = Color.White.copy(alpha = 0.6f),
                                    disabledSelectedColor = Color.White.copy(alpha = 0.3f),
                                    disabledUnselectedColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                            Text(
                                text = "Crear una\npregunta",
                                fontSize = 16.sp,
                                fontFamily = poppins,
                                color = Color.White.copy(alpha = if (selectedCard == "pregunta") 0.95f else 0.6f),
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Opción: Responder a otras preguntas
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable(enabled = selectedCard == "pregunta") {
                                    preguntaOption = "responder"
                                }
                        ) {
                            RadioButton(
                                selected = preguntaOption == "responder",
                                onClick = { preguntaOption = "responder" },
                                enabled = selectedCard == "pregunta",
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color.White,
                                    unselectedColor = Color.White.copy(alpha = 0.6f),
                                    disabledSelectedColor = Color.White.copy(alpha = 0.3f),
                                    disabledUnselectedColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                            Text(
                                text = "Responder a\notras preguntas",
                                fontSize = 16.sp,
                                fontFamily = poppins,
                                color = Color.White.copy(alpha = if (selectedCard == "pregunta") 0.95f else 0.6f),
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 🔹 ESPACIO ENTRE TARJETAS - Ajusta este valor según necesites (en dp)
            Spacer(modifier = Modifier.height(60.dp))

            // Tarjeta ACTIVIDAD
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(190.dp)
                    .clickable {
                        selectedCard = "actividad"
                        preguntaOption = null
                    }
                    .alpha(if (selectedCard == null || selectedCard == "actividad") 1f else 0.4f),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedCard == "actividad")
                        Color.Black
                    else
                        Color.Black.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Título en la parte superior (centrado)
                    Text(
                        text = "Actividad",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = poppins,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    // Opciones en la parte inferior
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Opción: Crear una actividad nueva
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable(enabled = selectedCard == "actividad") {
                                    actividadOption = "crear"
                                }
                        ) {
                            RadioButton(
                                selected = actividadOption == "crear",
                                onClick = { actividadOption = "crear" },
                                enabled = selectedCard == "actividad",
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color.White,
                                    unselectedColor = Color.White.copy(alpha = 0.6f),
                                    disabledSelectedColor = Color.White.copy(alpha = 0.3f),
                                    disabledUnselectedColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                            Text(
                                text = "Crear una\nactividad nueva",
                                fontSize = 16.sp,
                                fontFamily = poppins,
                                color = Color.White.copy(alpha = if (selectedCard == "actividad") 0.95f else 0.6f),
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Opción: Unirme a actividad existente
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable(enabled = selectedCard == "actividad") {
                                    actividadOption = "unirme"
                                }
                        ) {
                            RadioButton(
                                selected = actividadOption == "unirme",
                                onClick = { actividadOption = "unirme" },
                                enabled = selectedCard == "actividad",
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color.White,
                                    unselectedColor = Color.White.copy(alpha = 0.6f),
                                    disabledSelectedColor = Color.White.copy(alpha = 0.3f),
                                    disabledUnselectedColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                            Text(
                                text = "Unirme a\nactividad existente",
                                fontSize = 16.sp,
                                fontFamily = poppins,
                                color = Color.White.copy(alpha = if (selectedCard == "actividad") 0.95f else 0.6f),
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Botón circular de flecha
        val isEnabled = preguntaOption != null || actividadOption != null

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 40.dp, end = 28.dp)
                .size(75.dp)
                .clip(CircleShape)
                .background(if (isEnabled) Color.Black else Color.Black.copy(alpha = 0.25f))
                .clickable(enabled = isEnabled) {
                    when {
                        preguntaOption == "crear" -> {
                            navController.navigate(AppScreens.QuestionCreateScreen.route)
                        }
                        preguntaOption == "responder" -> {
                            navController.navigate(AppScreens.QuestionsListScreen.route)
                        }
                        actividadOption == "crear" -> {
                            navController.navigate(AppScreens.ActivityCreateScreen.route)
                        }
                        actividadOption == "unirme" -> {
                            navController.navigate(AppScreens.ActivitiesListScreen.route)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Continuar",
                tint = if (isEnabled) Color(0xFFFDD835) else Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(38.dp)
            )
        }
    }
}