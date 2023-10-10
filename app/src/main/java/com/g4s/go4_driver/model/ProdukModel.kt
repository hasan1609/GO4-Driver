package com.g4s.go4_driver.model

import com.google.gson.annotations.SerializedName

data class ProdukModel(

    @field:SerializedName("id_produk")
    val idProduk: String? = null,

    @field:SerializedName("nama_produk")
    val namaProduk: String? = null,

    @field:SerializedName("keterangan")
    val keterangan: String? = null,

    @field:SerializedName("terjual")
    val terjual: Any? = null,

    @field:SerializedName("harga")
    val harga: String? = null,

    @field:SerializedName("updated_at")
    val updatedAt: String? = null,

    @field:SerializedName("user_id")
    val userId: String? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("kategori")
    val kategori: String? = null,

    @field:SerializedName("foto_produk")
    val fotoProduk: String? = null,

    @field:SerializedName("status")
    val status: String? = null
)