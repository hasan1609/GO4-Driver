package com.g4s.go4_driver.model

import com.google.gson.annotations.SerializedName

data class ResponseCekBooking(

	@field:SerializedName("data")
	val data: OrderLogModel? = null,

	@field:SerializedName("status")
	val status: Boolean? = null,

	@field:SerializedName("jarak")
	val jarak: Int? = null,
)
