package com.g4s.go4_driver.model

import com.go4sumbergedang.go4.model.DetailRestoModel
import com.google.gson.annotations.SerializedName

data class ResponseBooking(

	@field:SerializedName("data")
	val data: OrderLogModel? = null,

	@field:SerializedName("jarak")
	val jarak: Int? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)
