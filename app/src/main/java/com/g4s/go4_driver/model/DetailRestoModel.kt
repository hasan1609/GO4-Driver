package com.go4sumbergedang.go4.model

import com.google.gson.annotations.SerializedName

data class DetailRestoModel(

    @field:SerializedName("jam_tutup")
    val jamTutup: String? = null,

    @field:SerializedName("latitude")
    val latitude: String? = null,

    @field:SerializedName("nama_resto")
    val namaResto: String? = null,

    @field:SerializedName("status_akun")
    val statusAkun: String? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("ttl")
    val ttl: String? = null,

    @field:SerializedName("status_toko")
    val statusToko: String? = null,

    @field:SerializedName("alamat")
    val alamat: String? = null,

    @field:SerializedName("jam_buka")
    val jamBuka: String? = null,

    @field:SerializedName("nik")
    val nik: String? = null,

    @field:SerializedName("id_detail")
    val idDetail: String? = null,

    @field:SerializedName("tempat_lahir")
    val tempatLahir: String? = null,

    @field:SerializedName("foto")
    val foto: String? = null,

    @field:SerializedName("updated_at")
    val updatedAt: String? = null,

    @field:SerializedName("user_id")
    val userId: String? = null,

    @field:SerializedName("longitude")
    val longitude: String? = null
)
