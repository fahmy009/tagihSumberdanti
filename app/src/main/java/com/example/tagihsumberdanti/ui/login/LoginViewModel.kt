package com.example.tagihsumberdanti.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tagihsumberdanti.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginResult {
    object Idle : LoginResult()
    object Loading : LoginResult()
    data class Success(val user: User) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

class LoginViewModel : ViewModel() {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> = _pin.asStateFlow()

    private val _loginResult = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val loginResult: StateFlow<LoginResult> = _loginResult.asStateFlow()

    fun onUsernameChange(value: String) {
        _username.value = value
    }

    fun onPinChange(value: String) {
        _pin.value = value
    }

    fun login() {
        val inputUsername = _username.value.trim()
        val inputPin = _pin.value.trim()

        if (inputUsername.isEmpty() || inputPin.isEmpty()) {
            _loginResult.value = LoginResult.Error("Username dan PIN tidak boleh kosong!")
            return
        }

        _loginResult.value = LoginResult.Loading

        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(inputUsername)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val user = document.toObject(User::class.java)?.copy(username = document.id)
                            if (user != null && user.pin == inputPin) {
                                _loginResult.value = LoginResult.Success(user)
                            } else {
                                _loginResult.value = LoginResult.Error("PIN salah!")
                            }
                        } else {
                            // Fallback for default local test users if Firestore collection is not seeded
                            handleFallbackLogin(inputUsername, inputPin)
                        }
                    }
                    .addOnFailureListener {
                        handleFallbackLogin(inputUsername, inputPin)
                    }
            } catch (e: Exception) {
                handleFallbackLogin(inputUsername, inputPin)
            }
        }
    }

    private fun handleFallbackLogin(inputUsername: String, inputPin: String) {
        if (inputUsername.lowercase() == "admin" && inputPin == "1234") {
            _loginResult.value = LoginResult.Success(User(username = "admin", pin = "1234", role = "Admin"))
        } else if ((inputUsername.lowercase() == "penagih" || inputUsername.lowercase() == "tukangtagih") && inputPin == "1234") {
            _loginResult.value = LoginResult.Success(User(username = inputUsername, pin = "1234", role = "Tukang Tagih"))
        } else {
            _loginResult.value = LoginResult.Error("Username atau PIN tidak cocok! (Default demo: admin/1234 atau penagih/1234)")
        }
    }

    fun resetState() {
        _loginResult.value = LoginResult.Idle
    }
}
