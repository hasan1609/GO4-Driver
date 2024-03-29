package com.g4s.go4_driver.model

data class ChatModel(
    val sender: String? = null,
    val message: String? = null,
    val receiver: String? = null,
    val isseen: Boolean = false,
    val url: String? = null,
    val messageId: String? = null,
)