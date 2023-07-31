package com.g4s.go4_driver.model

import com.google.gson.annotations.SerializedName

data class ResponseLogin(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null,

	@field:SerializedName("token")
	val token: String? = null
)

data class DetailDriver(

	@field:SerializedName("jk")
	val jk: String? = null,

	@field:SerializedName("kendaraan")
	val kendaraan: String? = null,

	@field:SerializedName("plat_no")
	val platNo: String? = null,

	@field:SerializedName("latitude")
	val latitude: Any? = null,

	@field:SerializedName("status_akun")
	val statusAkun: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("ttl")
	val ttl: String? = null,

	@field:SerializedName("alamat")
	val alamat: String? = null,

	@field:SerializedName("nik")
	val nik: String? = null,

	@field:SerializedName("status_driver")
	val statusDriver: String? = null,

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

	@field:SerializedName("thn_kendaraan")
	val thnKendaraan: String? = null,

	@field:SerializedName("longitude")
	val longitude: Any? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class Data(

	@field:SerializedName("detail_driver")
	val detailDriver: DetailDriver? = null,

	@field:SerializedName("fcm")
	val fcm: String? = null,

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("tlp")
	val tlp: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("email_verified_at")
	val emailVerifiedAt: Any? = null,

	@field:SerializedName("id_user")
	val idUser: String? = null,

	@field:SerializedName("email")
	val email: String? = null
)
