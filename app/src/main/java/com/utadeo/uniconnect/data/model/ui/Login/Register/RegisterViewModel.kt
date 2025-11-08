package com.utadeo.uniconnect.ui.register

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utadeo.uniconnect.data.manager.RegistrationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val registrationManager = RegistrationManager(context)

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    fun setEmail(email: String) {
        _email.value = email
    }

    /**
     * MODIFICADO: Ya NO crea la cuenta en Firebase Auth
     * Solo guarda email y password temporalmente
     */
    fun registerWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            try {
                _registerState.value = RegisterState.Loading

                // Guardar datos temporalmente (NO crear cuenta)
                registrationManager.saveEmail(email)
                registrationManager.savePassword(password)

                // Simular éxito (para que navegue al siguiente paso)
                _registerState.value = RegisterState.Success

            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }

    fun getTempData(): RegistrationManager.TempRegistrationData {
        return registrationManager.getTempData()
    }

    fun hasIncompleteRegistration(): Boolean {
        return registrationManager.hasIncompleteRegistration()
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}