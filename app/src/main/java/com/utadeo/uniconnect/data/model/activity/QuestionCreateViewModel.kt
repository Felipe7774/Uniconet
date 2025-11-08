package com.utadeo.uniconnect.data.model.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class QuestionCreateViewModel @Inject constructor() : ViewModel() {

    // Como QuestionsRepository es un object, accede directamente a él
    private val questionsRepository = QuestionsRepository

    /**
     * Publica una nueva pregunta en Firestore
     * @param questionText El texto de la pregunta
     * @param onSuccess Callback cuando se publica exitosamente
     * @param onError Callback cuando hay un error
     */
    fun publicarPregunta(
        questionText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                questionsRepository.addQuestion(questionText)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Error al publicar la pregunta")
            }
        }
    }
}