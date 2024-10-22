package com.vonabe.vibromessage.service

import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vonabe.vibromessage.data.RetrofitClient
import com.vonabe.vibromessage.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ReceiveEventService : FirebaseMessagingService() {

    companion object {
        const val TAG = "ReceiveEventService"
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onNewToken(fcmToken: String) {
        super.onNewToken(fcmToken)
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        serviceScope.launch {
            kotlin.runCatching {
                RetrofitClient.instance.authUser(
                    User(
                        currentUser.uid,
                        currentUser.getIdToken(false).result.token ?: "",
                        fcmToken
                    )
                )
            }.onSuccess {
                Log.d(TAG, "User registered successfully")
            }.onFailure {
                Log.d(TAG, "Registration failed -> ${it.message}")
            }
            Log.d(TAG, "NewToken $fcmToken")
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data.isNotEmpty()) {
            val intent = Intent(this, VibroService::class.java).apply {
                putExtra("message", message.data["message"])
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

}