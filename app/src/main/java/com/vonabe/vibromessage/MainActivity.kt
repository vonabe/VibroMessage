//package com.vonabe.vibromessage
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.content.ClipData
//import android.content.ClipboardManager
//import android.content.Context
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.os.Handler
//import android.os.Message
//import android.os.VibrationEffect
//import android.os.Vibrator
//import android.os.VibratorManager
//import android.provider.ContactsContract
//import android.util.Log
//import android.view.MotionEvent
//import android.view.View
//import android.widget.AdapterView
//import android.widget.ArrayAdapter
//import android.widget.TextView
//import android.widget.Toast
//import androidx.annotation.RequiresApi
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.google.android.gms.tasks.OnCompleteListener
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.messaging.FirebaseMessaging
//import com.vonabe.vibromessage.databinding.ActivityMainBinding
//
//
//class MainActivity : AppCompatActivity() {
//
//    companion object {
//        const val TAG: String = "VibroMessage -> MainActivity"
//    }
//
//    private lateinit var binding: ActivityMainBinding
//    private val friends = ArrayList<String>()
//    private var selectUserIDSender: String? = null
//
//    @SuppressLint("ClickableViewAccessibility")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val command = StringBuilder()
//
//        val handler = object : Handler(this.mainLooper) {
//            @RequiresApi(Build.VERSION_CODES.O)
//            override fun handleMessage(msg: Message) {
//                command.clear()
//
//                selectUserIDSender?.let {
//                    sendCommandToFriend(it, msg.obj as String)
//                }
//                vibrator.vibrate((msg.obj as String).commandToPattern())
//            }
//        }
//
//        var startTime = System.currentTimeMillis()
//        var endTime: Long
//        binding.btnClick.setOnTouchListener { v, event ->
//            if (event.action == MotionEvent.ACTION_DOWN) {
//                endTime = System.currentTimeMillis()
//                if (handler.hasMessages(1)) {
//                    val pause = endTime - startTime
//                    command.append("p:$pause").append("\n")
//                    handler.removeMessages(1)
//                }
//                startTime = System.currentTimeMillis()
//            } else if (event.action == MotionEvent.ACTION_UP) {
//                endTime = System.currentTimeMillis()
//                command.append("s:${endTime - startTime}").append("\n")
//                handler.sendMessageDelayed(Message().apply {
//                    obj = command.toString()
//                    what = 1
//                }, 1500)
//                startTime = System.currentTimeMillis()
//            }
//            false
//        }
//
//        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
//                return@OnCompleteListener
//            }
//
//            // Get new FCM registration token
//            val token = task.result
////            addFCM(token)
//
//            // Log and toast
//            val msg = getString(R.string.msg_token_fmt, token)
//            Log.d(TAG, msg)
////            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//        })
//
//        FirebaseAuth.getInstance().signInAnonymously()
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // Аутентификация успешна, получаем пользователя
//                    val user = task.result
//                    val fcmToken = FirebaseMessaging.getInstance().token
//
//                    Log.d("Auth", "User ID: ${user?.user?.uid}, fcmToken: ${fcmToken.result}")
////                    saveUserIdToFirestore() // Сохраняем ID в Firestore
//                } else {
//                    // Если аутентификация не удалась
//                    Log.w("Auth", "Authentication failed", task.exception)
//                }
//            }
//
////        checkUser()
//
//        binding.addUserButton.setOnClickListener {
//            val userId = binding.addUserEditText.text.toString()
//            if (userId.isNotBlank() && userId.length > 10) {
//                addFriend(userId)
//                binding.addUserEditText.text?.clear()
//            }
//        }
//
//        binding.userId.setOnClickListener {
//            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
//            val clip = ClipData.newPlainText("Copy UserId", (it as TextView).text)
//            clipboard.setPrimaryClip(clip)
//            Toast.makeText(this, "Copy text ${it.text}", Toast.LENGTH_SHORT).show()
//        }
//
//        binding.listContacts.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, friends)
//
//        binding.listContacts.onItemClickListener = object : AdapterView.OnItemClickListener {
//            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                selectUserIDSender = (view as TextView).text.toString()
//                Log.d(TAG, "select $selectUserIDSender")
//            }
//        }
////        getFriends()
//
//    }
//
//    fun addFCM(fcmToken: String) {
//        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
//        val db = FirebaseFirestore.getInstance()
//        // Обновляем документ пользователя в Firestore, добавляя токен
//        val userRef = db.collection("users").document(currentUser.uid)
//
//        userRef.update("fcmToken", fcmToken)
//            .addOnSuccessListener {
//                Log.d("Firestore", "FCM token successfully saved")
//            }
//            .addOnFailureListener { e ->
//                Log.w("Firestore", "Error saving FCM token", e)
//            }
//    }
//
//    fun addFriend(friendUserId: String) {
//        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
//        val db = FirebaseFirestore.getInstance()
//
//        // Добавляем друга в подколлекцию friends текущего пользователя
//        val friendMap = hashMapOf("friendUserId" to friendUserId)
//
//        db.collection("users")
//            .document(currentUser.uid)
//            .collection("friends")
//            .document(friendUserId)
//            .set(friendMap)
//            .addOnSuccessListener {
//                Log.d("Firestore", "Friend added successfully")
//                getFriends()
//            }
//            .addOnFailureListener { e ->
//                Log.w("Firestore", "Error adding friend", e)
//            }
//    }
//
//    fun removeFriend(friendUserId: String) {
//        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
//        val db = FirebaseFirestore.getInstance()
//
//        db.collection("users")
//            .document(currentUser.uid)
//            .collection("friends")
//            .document(friendUserId)
//            .delete()
//            .addOnSuccessListener {
//                Log.d("Firestore", "Friend removed successfully")
//                getFriends()
//            }
//            .addOnFailureListener { e ->
//                Log.w("Firestore", "Error removing friend", e)
//            }
//    }
//
//    fun getFriends() {
//        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
//        val db = FirebaseFirestore.getInstance()
//
//        db.collection("users")
//            .document(currentUser.uid)
//            .collection("friends")
//            .get()
//            .addOnSuccessListener { result ->
//                val fr = result.mapNotNull { it.getString("friendUserId") }.toList()
//                friends.clear()
//                friends.addAll(fr)
//                binding.listContacts.invalidateViews()
////                for (document in result) {
////                    val friendUserId = document.getString("friendUserId")
////                    Log.d("Firestore", "Friend User ID: $friendUserId")
////                }
//            }
//            .addOnFailureListener { e ->
//                Log.w("Firestore", "Error getting friends", e)
//            }
//    }
//
//    fun sendCommandToFriend(friendUserId: String, command: String) {
//        val db = FirebaseFirestore.getInstance()
//
//        val commandMap = hashMapOf(
//            "command" to command,
//            "timestamp" to System.currentTimeMillis()
//        )
//
//        db.collection("users")
//            .document(friendUserId)
//            .collection("commands")
//            .add(commandMap)
//            .addOnSuccessListener {
//                Log.d("Firestore", "Command sent successfully")
//            }
//            .addOnFailureListener { e ->
//                Log.w("Firestore", "Error sending command", e)
//            }
//    }
//
//    private fun authenticateAnonymously() {
//        val auth = FirebaseAuth.getInstance()
//
//        auth.signInAnonymously()
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // Аутентификация успешна, получаем пользователя
//                    val user = auth.currentUser
//                    Log.d("Auth", "User ID: ${user?.uid}")
//                    saveUserIdToFirestore() // Сохраняем ID в Firestore
//                } else {
//                    // Если аутентификация не удалась
//                    Log.w("Auth", "Authentication failed", task.exception)
//                }
//            }
//    }
//
//    private fun checkUser() {
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        if (currentUser != null) {
//            // Пользователь аутентифицирован
//            Log.d("Auth", "User ID: ${currentUser.uid}")
//            saveUserIdToFirestore() // Сохраняем ID в Firestore
//        } else {
//            // Пользователь не аутентифицирован, запускаем анонимную аутентификацию
//            authenticateAnonymously()
//        }
//    }
//
//    private fun saveUserIdToFirestore() {
//        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
//        val db = FirebaseFirestore.getInstance()
//
//        val userMap = hashMapOf("userId" to currentUser.uid)
//
//        db.collection("users").document(currentUser.uid).set(userMap)
//            .addOnSuccessListener {
//                Log.d("Firestore", "User ID saved successfully")
//                binding.userId.text = currentUser.uid
//                Toast.makeText(this, "User ID saved successfully", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener { e ->
//                Log.w("Firestore", "Error saving user ID", e)
//            }
//    }
//
//    private fun requestPermissionContacts() {
//        // Запрос разрешений на чтение контактов
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 1)
//        } else {
//            // Разрешение уже предоставлено
//            getContacts()
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        if (requestCode == 1) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Разрешение предоставлено
//                getContacts()
//            }
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }
//
//    private fun getContacts() {
//        val contentResolver = contentResolver
//        val cursor = contentResolver.query(
//            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//            null, null, null, null
//        )
//
//        cursor?.use {
//            if (it.count > 0) {
//                val hashMap = ArrayList<String>()
//                while (it.moveToNext()) {
//                    // Получаем имя контакта
//                    val name =
//                        it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
//                    // Получаем номер телефона контакта
//                    val phoneNumber =
//                        it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
//                    hashMap.add("$name: $phoneNumber")
//                    // Выводим имя и номер телефона в лог или используем в приложении
//                    Log.d("Contact", "Name: $name, Phone Number: $phoneNumber")
//                }
//
//                binding.listContacts.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, hashMap)
//            }
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun String.commandToPattern(): VibrationEffect? {
//        val commandList = this.split("\n")
//        val data = LongArray(commandList.size + 2)
//        data[0] = 0L
//        for ((count, index) in (1..<commandList.size).withIndex()) {
//            val it = commandList[count]
//            data[index] =
//                if (it.contains("s:")) it.split("s:").last().toLong() else it.split("p:").last().toLong()
//        }
//        data[data.size - 1] = 0L
//        return VibrationEffect.createWaveform(data, -1)
//    }
//
//    // Get Vibrator service
//    private val vibrator: Vibrator by lazy {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
//            vibratorManager.defaultVibrator
//        } else {
//            @Suppress("DEPRECATION")
//            getSystemService(VIBRATOR_SERVICE) as Vibrator
//        }
//    }
//
//}