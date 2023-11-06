package com.g4s.go4_driver.ui.activity

import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.g4s.go4_driver.R
import com.g4s.go4_driver.adapter.ItemLogOrderRestoAdapter
import com.g4s.go4_driver.databinding.ActivityDetailRiwayatOrderBinding
import com.g4s.go4_driver.model.ProdukOrderModel
import com.g4s.go4_driver.model.ResponseCekBooking
import com.g4s.go4_driver.model.ResponseDetailLogOrder
import com.g4s.go4_driver.model.ResponsePostData
import com.g4s.go4_driver.session.SessionManager
import com.g4s.go4_driver.utils.AlertOrderUtils
import com.g4s.go4_driver.webservice.ApiClient
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.bottomsheet_data_customer.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class DetailRiwayatOrderActivity : AppCompatActivity() , AnkoLogger {
    private lateinit var binding: ActivityDetailRiwayatOrderBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var sessionManager: SessionManager
    var api = ApiClient.instance()
    private lateinit var mAdapterProduk: ItemLogOrderRestoAdapter
    private val produkItems = mutableListOf<ProdukOrderModel>()
    companion object {
        const val idOrder = "idOrder"
    }
    private lateinit var alertOrderUtils: AlertOrderUtils

    // Register receiver for location updates
    private val orderReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val responseDataJson = intent!!.getStringExtra("response_data")
            val responseData = Gson().fromJson(responseDataJson, ResponseCekBooking::class.java)
            alertOrderUtils.showAlertDialog(responseData)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_riwayat_order)
        binding.lifecycleOwner = this
        sessionManager = SessionManager(this)
        progressDialog = ProgressDialog(this)
        val data = intent.getStringExtra(idOrder)
        binding.toolbar.titleTextView.text = "Detail Order"
        binding.toolbar.backButton.setOnClickListener{
            finish()
        }
        binding.btnSelesai.setOnClickListener {
            updateStatus("5", data.toString())
        }
        val filter2 = IntentFilter("CEK_BOOKING")
        this.registerReceiver(orderReceiver, filter2)
        getData(data.toString())
    }

    private fun getData(idOrder: String) {
        loading(true)
        binding.rvProduk.layoutManager = LinearLayoutManager(this)
        binding.rvProduk.setHasFixedSize(true)
        (binding.rvProduk.layoutManager as LinearLayoutManager).orientation =
            LinearLayoutManager.VERTICAL

        api.getDetailOrderLog(idOrder).enqueue(object : Callback<ResponseDetailLogOrder> {
            override fun onResponse(
                call: Call<ResponseDetailLogOrder>,
                response: Response<ResponseDetailLogOrder>
            ) {
                try {
                    if (response.isSuccessful) {
                        loading(false)
                        val data = response.body()
                        val formatter = DecimalFormat.getCurrencyInstance() as DecimalFormat
                        val symbols = formatter.decimalFormatSymbols
                        symbols.currencySymbol = "Rp. "
                        formatter.decimalFormatSymbols = symbols
                        val totalx = data!!.order!!.total!!.toDoubleOrNull() ?: 0.0
                        val ongkir = data.order!!.ongkosKirim!!.toDoubleOrNull() ?: 0.0

                        if (data.produk == null){
                            binding.tvOngkir.text = "Harga Ojek"
                            binding.rvProduk.visibility = View.GONE
                            binding.lySpace2.visibility = View.GONE
                            binding.lyResto.visibility = View.GONE
                        }else{
                            binding.txtSubtotal.text = formatter.format(totalx)
                            binding.namaResto.text = data.order.detailResto!!.namaResto.toString()
                            binding.namaDriver.text = data.order.driver!!.nama.toString()
                            for (hasil in data.produk) {
                                produkItems.add(hasil!!)
                                mAdapterProduk = ItemLogOrderRestoAdapter(produkItems, this@DetailRiwayatOrderActivity)
                                binding.rvProduk.adapter = mAdapterProduk
                                mAdapterProduk.notifyDataSetChanged()
                            }
                        }
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
                        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                        val date = dateFormat.parse(data.order.createdAt.toString())
                        val formattedDate = SimpleDateFormat("dd MMM yyyy, HH:mm:ss").format(date!!)
                        binding.txtTgl.text = formattedDate
                        binding.txtLokasiAntar.text = data.order.alamatTujuan.toString()
                        binding.txtLokasiJemput.text = data.order.alamatDari.toString()
                        binding.txtOngkir.text = formatter.format(ongkir)

                        when (data.order.status) {
                            "0" -> {
                                binding.txtStatus.text = "Menunggu Konfirmasi"
                                binding.txtStatus.setTextColor(getColor(R.color.primary_color))
                            }
                            "1" -> {
                                when(data.order.kategori){
                                    "resto" ->{
                                        binding.txtStatus.text = "Menuju Lokasi Resto"
                                    }
                                    else -> {
                                        binding.txtStatus.text = "Menuju Lokasi Jemput"
                                    }
                                }
                                binding.txtStatus.setTextColor(getColor(R.color.primary_color))
                            }
                            "2" -> {
                                when(data.order.kategori){
                                    "resto" ->{
                                        binding.txtStatus.text = "Sampai Lokasi Resto"
                                    }
                                    else -> {
                                        binding.txtStatus.text = "Sampai Lokasi Jemput"
                                    }
                                }
                                binding.txtStatus.setTextColor(getColor(R.color.primary_color))
                            }
                            "3" -> {
                                binding.txtStatus.text = "Menuju Lokasi Tujuan"
                                binding.txtStatus.setTextColor(getColor(R.color.primary_color))
                            }
                            "4" -> {
                                binding.txtStatus.text = "Sampai Lokasi Tujuan"
                                binding.txtStatus.setTextColor(getColor(R.color.primary_color))
                                binding.btnSelesai.visibility = View.VISIBLE
                            }
                            "5" -> {
                                binding.txtStatus.text = "Selesai"
                                binding.txtStatus.setTextColor(getColor(R.color.teal_700))
                            }
                            "7" -> {
                                binding.txtStatus.text = "Order Diterima"
                                binding.txtStatus.setTextColor(getColor(R.color.primary_color))
                            }
                            else -> {
                                binding.txtStatus.text = "Ditolak"
                                binding.txtStatus.setTextColor(getColor(R.color.red))
                            }
                        }

                    } else {
                        loading(false)
                        toast("gagal mendapatkan response")
                    }
                } catch (e: Exception) {
                    loading(false)
                    info { "hasan ${e.message}" }
                    toast(e.message.toString())
                }
            }
            override fun onFailure(call: Call<ResponseDetailLogOrder>, t: Throwable) {
                loading(false)
                info { "hasan ${t.message}" }
                toast(t.message.toString())
            }
        })
    }
    private fun deleteLocationDataFromDatabase() {
        val database = FirebaseDatabase.getInstance()
        val cartReference = database.reference.child("perjalanan_pengemudi").child(sessionManager.getId().toString())
        cartReference.removeValue()
            .addOnSuccessListener {
                loading(false)
            }
            .addOnFailureListener { exception ->
                loading(false)
                toast("gagal mematikan")
            }
    }
    private fun updateStatus(status: String, idOrder: String){
        loading(true)
        api.updateStatusOrder(idOrder, status).enqueue(object :
            Callback<ResponsePostData> {
            override fun onResponse(
                call: Call<ResponsePostData>,
                response: Response<ResponsePostData>
            ) {
                try {
                    if (response.isSuccessful) {
                        loading(false)
                        binding.btnSelesai.visibility = View.GONE
                        binding.txtStatus.text = "Selesai"
                        binding.txtStatus.setTextColor(getColor(R.color.teal_700))
                        deleteLocationDataFromDatabase()
                    } else {
                        loading(false)
                        toast("gagal mendapatkan response")
                    }
                } catch (e: Exception) {
                    loading(false)
                    info { "hasan ${e.message}" }
                    toast(e.message.toString())
                }
            }
            override fun onFailure(call: Call<ResponsePostData>, t: Throwable) {
                loading(false)
                info { "hasan ${t.message}" }
                toast(t.message.toString())
            }
        })
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            progressDialog.setMessage("Tunggu sebentar...")
            progressDialog.setCancelable(false)
            progressDialog.show()
        } else {
            progressDialog.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(orderReceiver)
    }
}