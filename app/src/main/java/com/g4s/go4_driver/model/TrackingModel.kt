package com.g4s.go4_driver.model

import com.google.gson.annotations.SerializedName

data class TrackingModel (
    @field:SerializedName("latitude")
    val latitude: Double? = null,

    @field:SerializedName("longitude")
    val longitude: Double? = null,

)