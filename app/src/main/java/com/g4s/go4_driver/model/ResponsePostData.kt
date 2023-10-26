package com.g4s.go4_driver.model

import com.google.gson.annotations.SerializedName

data class ResponsePostData(

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: Boolean? = null
)
