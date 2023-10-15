package com.g4s.go4_driver.model

import java.sql.ClientInfoStatus

data class LokasiDriverModel (
//    val uuid: String,
    val latitude: Double,
    val longitude: Double,
    val type: String,
    val status: String
)