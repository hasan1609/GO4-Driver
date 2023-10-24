package com.g4s.go4_driver.model

import com.google.gson.annotations.SerializedName

data class SaldoModel (

    @field:SerializedName("id_saldo")
    val idSaldo: String? = null,

    @field:SerializedName("user_id")
    val userId: String? = null,

    @field:SerializedName("saldo")
    val saldo: String? = null,

    @field:SerializedName("updated_at")
    val updatedAt: String? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,
)