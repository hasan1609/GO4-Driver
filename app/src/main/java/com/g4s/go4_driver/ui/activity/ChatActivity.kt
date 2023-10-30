package com.g4s.go4_driver.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.g4s.go4_driver.R
import com.g4s.go4_driver.adapter.DetailChatAdapter
import com.g4s.go4_driver.databinding.ActivityChatBinding
import com.g4s.go4_driver.session.SessionManager

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var mAdapter: DetailChatAdapter
    private lateinit var chatList: MutableList<ChatModel>
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        binding.lifecycleOwner = this
        sessionManager = SessionManager(this)
        chatList = mutableListOf()
        mAdapter = DetailChatAdapter(this, chatList, sessionManager.getId().toString())
    }
}