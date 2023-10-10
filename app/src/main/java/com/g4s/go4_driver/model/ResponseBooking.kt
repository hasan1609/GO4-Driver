package com.g4s.go4_driver.model

import com.go4sumbergedang.go4.model.DetailRestoModel
import com.google.gson.annotations.SerializedName

data class ResponseBooking(

	@field:SerializedName("data")
	val data: BookingModel? = null,

	@field:SerializedName("jarak")
	val jarak: Int? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)

data class BookingModel(

	@field:SerializedName("driver_id")
	val driverId: Any? = null,

	@field:SerializedName("produk_order")
	val produkOrder: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("biaya_pesanan")
	val biayaPesanan: String? = null,

	@field:SerializedName("detail_resto")
	val detailResto: DetailRestoModel? = null,

	@field:SerializedName("id_booking")
	val idBooking: String? = null,

	@field:SerializedName("resto_id")
	val restoId: String? = null,

	@field:SerializedName("alamat_tujuan")
	val alamatTujuan: String? = null,

	@field:SerializedName("total")
	val total: String? = null,

	@field:SerializedName("latitude_tujuan")
	val latitudeTujuan: String? = null,

	@field:SerializedName("ongkos_kirim")
	val ongkosKirim: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("customer_id")
	val customerId: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("longitude_tujuan")
	val longitudeTujuan: String? = null
)
