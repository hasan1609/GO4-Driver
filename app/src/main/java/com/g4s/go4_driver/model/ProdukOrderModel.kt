package com.g4s.go4_driver.model

import com.google.gson.annotations.SerializedName

data class ProdukOrderModel(

    @field:SerializedName("total")
    val total: String? = null,

    @field:SerializedName("jumlah")
    val jumlah: String? = null,

    @field:SerializedName("updated_at")
    val updatedAt: String? = null,

    @field:SerializedName("produk")
    val produk: ProdukModel? = null,

    @field:SerializedName("user_id")
    val userId: String? = null,

    @field:SerializedName("catatan")
    val catatan: Any? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("produk_id")
    val produkId: String? = null,

    @field:SerializedName("id_order_resto")
    val idOrderResto: String? = null,

    @field:SerializedName("toko_id")
    val tokoId: String? = null
)