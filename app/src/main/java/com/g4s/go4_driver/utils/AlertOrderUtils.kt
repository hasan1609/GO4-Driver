package com.g4s.go4_driver.utils

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.g4s.go4_driver.R
import com.g4s.go4_driver.model.OrderLogModel
import com.g4s.go4_driver.model.ResponseCekBooking
import com.g4s.go4_driver.model.ResponsePostData
import com.g4s.go4_driver.ui.activity.TrackingOrderActivity
import com.g4s.go4_driver.webservice.ApiClient
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.bottomsheet_data_customer.view.*
import kotlinx.android.synthetic.main.custom_alert_recive_booking.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AlertOrderUtils(private val context: Context){
    val api = ApiClient.instance()
    fun showAlertDialog(order: ResponseCekBooking) {
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_recive_booking, null)
        val txtKode = dialogView.txt_kode
        val txtJenisOrder = dialogView.txt_jenis_order
        val txtTotal = dialogView.txt_total
        val txtJarak = dialogView.txt_jarak
        val txtTujuan = dialogView.txt_tujuan
        val btnTerima = dialogView.btn_terima
        val btnTolak = dialogView.btn_tolak

        txtKode.text = order.data!!.idOrder
        txtJenisOrder.text = order.data.kategori.toString().toUpperCase()
        txtTotal.text = order.data.total
        txtJarak.text = "±" + order.jarak.toString() + "KM"
        txtTujuan.text = order.data.alamatTujuan

        // Menambahkan view yang telah diinisialisasi ke dalam builder
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.show()

        // Mengatur aksi ketika tombol Terima diklik
        btnTerima.setOnClickListener {
            alertDialog.dismiss() // Tutup alert dialog setelah aksi selesai
            updateStatus(order.data.idOrder.toString(), "7", order.data)
        }

        // Mengatur aksi ketika tombol Tolak diklik
        btnTolak.setOnClickListener {
            alertDialog.dismiss() // Tutup alert dialog setelah aksi selesai
            // Lakukan aksi yang diinginkan saat tombol Tolak diklik
            updateStatus(order.data.idOrder.toString(), "6", order.data)

        }
    }

    private fun updateStatus(id: String, status: String, orderLog: OrderLogModel){
        api.updateStatusOrder(id, status).enqueue(object :
            Callback<ResponsePostData> {
            override fun onResponse(
                call: Call<ResponsePostData>,
                response: Response<ResponsePostData>
            ) {
                try {
                    if (response.isSuccessful) {
                        if (status == "7"){
//                            val gson = Gson()
//                            val orderJson = gson.fromJson(orderLog.toString(), OrderLogModel::class.java)
//                            context.startActivity<TrackingOrderActivity>("order" to orderJson)
                            Toast.makeText(context, "Order Diterima", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(context, "Order Ditolak", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.d("hasan", e.message.toString())
                    Log.e("ahhhh", status)
                    Toast.makeText(context, "Kesalahan Response", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ResponsePostData>, t: Throwable) {
                Log.d("hasan", t.message.toString())
                Toast.makeText(context, "Kesalahan Response", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
