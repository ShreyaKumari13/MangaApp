package com.example.androiddevelopmenttask.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androiddevelopmenttask.domain.model.Result
import com.example.androiddevelopmenttask.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Initial)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    fun onEmailChanged(email: String) {
        _email.value = email
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _confirmPassword.value = confirmPassword
    }

    fun register() {
        if (_password.value != _confirmPassword.value) {
            _registerState.value = RegisterState.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            
            when (val result = registerUseCase(_email.value, _password.value)) {
                is Result.Success -> {
                    _registerState.value = RegisterState.Success
                }
                is Result.Error -> {
                    _registerState.value = RegisterState.Error(result.message)
                }
                is Result.Loading -> {
                    _registerState.value = RegisterState.Loading
                }
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Initial
    }
}

sealed class RegisterState {
    object Initial : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}
