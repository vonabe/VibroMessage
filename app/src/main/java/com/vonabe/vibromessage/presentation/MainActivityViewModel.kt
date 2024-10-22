package com.vonabe.vibromessage.presentation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.vonabe.vibromessage.data.RetrofitClient
import com.vonabe.vibromessage.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {
    private val TAG = javaClass.simpleName

    private val _uiState = MutableStateFlow(MainState())
    val uiState: StateFlow<MainState> = _uiState.asStateFlow()
    val errorState = MutableLiveData<String>()
    val messageState = MutableLiveData<String>()

    fun authentication() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Пользователь уже аутентифицирован, можно получить FCM токен
            getFCMToken()
        } else {
            // Пользователь не аутентифицирован, выполнить анонимную авторизацию
            FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    getFCMToken()
                } else {
                    errorState.value = task.exception?.message
                    Log.w(TAG, "signInAnonymously:failure", task.exception)
                }
            }
        }
    }

    fun addFriend(userId: String, friendId: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                RetrofitClient.instance.addFriend(userId, friendId)
            }.onSuccess {
                Log.d(TAG, "Response ${it.message}")
                messageState.value = it.message
                getFCMToken()
            }.onFailure {
                errorState.value = it.message
                Log.d(TAG, "ResponseError ${it.message}")
            }
        }
    }

    fun sendMessage(userId: String, friendId: String, message: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                RetrofitClient.instance.sendMessage(userId, friendId, message)
            }.onSuccess { message ->
                messageState.value = message.message
            }.onFailure {
                errorState.value = it.message
            }
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                // Отправьте токен на сервер
                val currentUser = FirebaseAuth.getInstance().currentUser
                currentUser?.getIdToken(false)?.addOnCompleteListener {
                    val idToken = it.result.token
                    val userId = currentUser.uid
                    requestAuth(fcmToken!!, idToken!!, userId)
                }
            } else {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                errorState.value = task.exception?.message ?: "Fetching FCM registration token failed"
            }
        }
    }

    // Ваш ViewModel метод
    private fun requestAuth(fcmToken: String, authToken: String, userId: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                RetrofitClient.instance.authUser(User(userId, authToken, fcmToken))
            }.onSuccess { user ->
                Log.d(TAG, "AuthResponse: $user")
                _uiState.update {
                    MainState(user)
                }
            }.onFailure {
                errorState.value = it.message
            }
        }
    }

}