package com.go4sumbergedang.go4.model

import com.google.gson.annotations.SerializedName

data class DetailCustomerModel(

	@field:SerializedName("id_detail")
	val idDetail: String? = null,

	@field:SerializedName("foto")
	val foto: Any? = null,

	@field:SerializedName("updated_at")
	val updatedAt: Any? = null,

	@field:SerializedName("user_id")
	val userId: String? = null,

	@field:SerializedName("latitude")
	val latitude: Any? = null,

	@field:SerializedName("created_at")
	val createdAt: Any? = null,

	@field:SerializedName("alamat")
	val alamat: String? = null,

	@field:SerializedName("longitude")
	val longitude: Any? = null,

	@field:SerializedName("status")
	val status: String? = null
)
