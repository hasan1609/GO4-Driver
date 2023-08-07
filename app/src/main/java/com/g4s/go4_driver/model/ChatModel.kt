package com.g4s.go4_driver.model

class ChatModel {
    var pesan: String? = null
    var senderId: String? = null

    constructor(){}

    constructor(pesan: String?, senderId: String?){
        this.pesan = pesan
        this.senderId = senderId
    }
}