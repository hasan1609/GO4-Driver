package com.g4s.go4_driver.model

import com.google.gson.annotations.SerializedName

data class ResponseDetailLogOrder(

    @field:SerializedName("produk")
    val produk: List<ProdukOrderModel?>? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("totalJumlah")
    val totalJumlah: Int? = null,

    @field:SerializedName("status")
    val status: Boolean? = null,

    @field:SerializedName("order")
    val order: OrderLogModel? = null
)