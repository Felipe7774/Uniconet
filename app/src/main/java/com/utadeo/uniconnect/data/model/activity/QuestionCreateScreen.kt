package com.utadeo.uniconnect.data.model.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.data.model.activity.QuestionCreateViewModel
import com.utadeo.uniconnect.data.model.navigation.AppScreens

@Composable
fun QuestionCreateScreen(
    navController: NavController,
    viewModel: QuestionCreateViewModel = hiltViewModel()
) {
    val poppins = FontFamily(Font(R.font.poppins_regular))

    var questionText by remember { mutableStateOf("") }
    var isPublishing by remember { mutableStateOf(false) }
    val maxCharacters = 100
    val minCharacters = 10

    val isValidQuestion = remember(questionText) {
        validateQuestion(questionText, minCharacters)
    }

    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDD835))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Botón de volver
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Título
            Text(
                text = "Formula tu pregunta",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = poppins,
                color = Color.Black,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Card con el TextField
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 400.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    TextField(
                        value = questionText,
                        onValueChange = {
                            if (it.length <= maxCharacters) {
                                questionText = it
                                showErrorMessage = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 20.sp,
                            fontFamily = poppins,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 28.sp
                        ),
                        placeholder = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "¿Suelen pensar\nmucho en\nsu futuro?",
                                    fontSize = 20.sp,
                                    fontFamily = poppins,
                                    color = Color.White.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 28.sp
                                )
                            }
                        },
                        enabled = !isPublishing
                    )

                    // Contador de caracteres
                    Text(
                        text = "${questionText.length}/$maxCharacters",
                        fontSize = 14.sp,
                        fontFamily = poppins,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mensaje de error
            if (showErrorMessage && errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "⚠️ $errorMessage",
                        fontSize = 14.sp,
                        fontFamily = poppins,
                        color = Color.Red.copy(red = 0.8f),
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Indicadores de validación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ValidationIndicator(
                    text = "Mínimo $minCharacters caracteres",
                    isValid = questionText.length >= minCharacters,
                    poppins = poppins
                )
                ValidationIndicator(
                    text = "Texto válido",
                    isValid = hasValidWords(questionText),
                    poppins = poppins
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón Publicar
            Button(
                onClick = {
                    if (isValidQuestion && !isPublishing) {
                        isPublishing = true
                        viewModel.publicarPregunta(
                            questionText = questionText,
                            onSuccess = {
                                // Pregunta publicada exitosamente - ir a Home sin poder volver
                                isPublishing = false
                                navController.navigate(AppScreens.HomeScreen.route) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onError = { error ->
                                // Error al publicar
                                isPublishing = false
                                showErrorMessage = true
                                errorMessage = error
                            }
                        )
                    } else if (!isValidQuestion) {
                        showErrorMessage = true
                        errorMessage = when {
                            questionText.length < minCharacters ->
                                "La pregunta debe tener al menos $minCharacters caracteres"
                            !hasValidWords(questionText) ->
                                "La pregunta debe contener palabras válidas, no solo caracteres aleatorios"
                            else -> "La pregunta no es válida"
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .height(50.dp)
                    .widthIn(min = 140.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Black.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(25.dp),
                enabled = isValidQuestion && !isPublishing
            ) {
                if (isPublishing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Publicando...",
                        fontSize = 18.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "Publicar",
                        fontSize = 18.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

private fun validateQuestion(text: String, minChars: Int): Boolean {
    if (text.isBlank() || text.length < minChars) return false
    if (!hasValidWords(text)) return false
    return true
}

private fun hasValidWords(text: String): Boolean {
    if (text.isBlank()) return false

    val vowels = text.count { it.lowercaseChar() in "aeiouáéíóúü" }
    val consonants = text.count { it.lowercaseChar() in "bcdfghjklmnpqrstvwxyzñ" }
    val totalLetters = vowels + consonants

    if (totalLetters == 0) return false

    val vowelRatio = vowels.toFloat() / totalLetters
    if (vowelRatio < 0.20f) return false

    val uniqueChars = text.lowercase().filter { it.isLetter() }.toSet().size
    if (uniqueChars < 3) return false

    val words = text.trim().split(Regex("\\s+"))
    if (words.size < 2 && text.length > 15) return false

    return true
}

@Composable
private fun ValidationIndicator(
    text: String,
    isValid: Boolean,
    poppins: FontFamily
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = if (isValid) "✓" else "○",
            fontSize = 16.sp,
            color = if (isValid) Color(0xFF4CAF50) else Color.Black.copy(alpha = 0.3f)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            fontFamily = poppins,
            color = if (isValid) Color.Black else Color.Black.copy(alpha = 0.5f)
        )
    }
}