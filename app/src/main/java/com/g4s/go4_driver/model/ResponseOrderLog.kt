package com.g4s.go4_driver.model

import com.google.gson.annotations.SerializedName

data class ResponseOrderLog(

	@field:SerializedName("data")
	val data: List<DataLogOrder?>? = null,

	@field:SerializedName("pendapatan")
	val pendapatan: Int? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)

data class DataLogOrder(

	@field:SerializedName("count")
	val count: Int? = null,

	@field:SerializedName("order")
	val order: OrderLogModel? = null
)

