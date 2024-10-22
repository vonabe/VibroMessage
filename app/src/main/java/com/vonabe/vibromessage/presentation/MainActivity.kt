package com.vonabe.vibromessage.presentation

import android.R
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vonabe.vibromessage.data.model.UserResponse
import com.vonabe.vibromessage.databinding.ActivityMainBinding
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG: String = "VibroMessage -> MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private var selectUserIDSender: String? = null

    private var userInfo: UserResponse? = null
    private val friends = ArrayList<String>()

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        Open Disable Optimization apps
//        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
//        startActivity(intent)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setup()

        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)

        binding.listContacts.adapter = ArrayAdapter(this, R.layout.simple_list_item_1, friends)
        binding.listContacts.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            selectUserIDSender = (view as TextView).text.toString()
            Log.d(TAG, "select $selectUserIDSender")
        }

        binding.addUserButton.setOnClickListener {
            val userId = binding.addUserEditText.text.toString()
            if (userId.isNotBlank() && userId.length > 10) {
                userInfo?.let { userInfo -> viewModel.addFriend(userInfo.uniqueId, userId) }
                binding.addUserEditText.text?.clear()
            }
        }

        binding.userId.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copy UserId", (it as TextView).text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copy text ${it.text}", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { it ->
                    this@MainActivity.userInfo = it.userInfo
                    it.userInfo?.let {
                        friends.clear()
                        it.friends?.let { friends -> this@MainActivity.friends.addAll(friends) }
                        binding.listContacts.invalidateViews()
                        binding.userId.text = it.uniqueId
                    }
                }
            }
        }

        viewModel.messageState.observe(this@MainActivity) {
            Toast.makeText(this@MainActivity, it ?: "response empty", Toast.LENGTH_SHORT).show()
        }

        viewModel.errorState.observe(this@MainActivity) {
            Toast.makeText(this@MainActivity, it ?: "error empty", Toast.LENGTH_SHORT).show()
        }

        viewModel.authentication()
    }

    private fun sendCommand(userId: String, message: String) {
        userInfo?.let {
            viewModel.sendMessage(it.uniqueId, userId, message)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setup() {
        val command = StringBuilder()

        val handler = object : Handler(this.mainLooper) {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun handleMessage(msg: Message) {
                command.clear()

                selectUserIDSender?.let {
                    sendCommand(it, msg.obj as String)
//                    sendCommandToFriend(it, msg.obj as String)
                }
//                vibrator.vibrate((msg.obj as String).commandToPattern())
            }
        }

        var startTime = System.currentTimeMillis()
        var endTime: Long
        binding.btnClick.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                endTime = System.currentTimeMillis()
                if (handler.hasMessages(1)) {
                    val pause = endTime - startTime
                    command.append("p:$pause").append("\n")
                    handler.removeMessages(1)
                }
                startTime = System.currentTimeMillis()
            } else if (event.action == MotionEvent.ACTION_UP) {
                endTime = System.currentTimeMillis()
                command.append("s:${endTime - startTime}").append("\n")
                handler.sendMessageDelayed(Message().apply {
                    obj = command.toString()
                    what = 1
                }, 1500)
                startTime = System.currentTimeMillis()
            }
            false
        }
    }

    // Get Vibrator service
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION") getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

}