package com.g4s.go4_driver.ui.fragment

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.g4s.go4_driver.R
import com.g4s.go4_driver.adapter.RiwayatOrderAdapter
import com.g4s.go4_driver.databinding.FragmentPendapatanBinding
import com.g4s.go4_driver.model.DataLogOrder
import com.g4s.go4_driver.model.OrderLogModel
import com.g4s.go4_driver.model.ResponseOrderLog
import com.g4s.go4_driver.session.SessionManager
import com.g4s.go4_driver.ui.activity.DetailRiwayatOrderActivity
import com.g4s.go4_driver.ui.activity.TrackingOrderActivity
import com.g4s.go4_driver.webservice.ApiClient
import com.google.gson.Gson
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.time.LocalDate
import java.util.*

class PendapatanFragment : Fragment(), AnkoLogger {

    private lateinit var binding: FragmentPendapatanBinding
    var api = ApiClient.instance()
    private lateinit var mAdapter: RiwayatOrderAdapter
    lateinit var sessionManager: SessionManager
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pendapatan, container, false)
        binding.lifecycleOwner = this
        sessionManager = SessionManager(requireActivity())
        progressDialog = ProgressDialog(requireActivity())
        binding.txtTgl.text = LocalDate.now().toString()
        return binding.root
    }

    private fun getData(userId: String) {
        binding.rvRiwayat.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvRiwayat.setHasFixedSize(true)
        (binding.rvRiwayat.layoutManager as LinearLayoutManager).orientation =
            LinearLayoutManager.VERTICAL
        loading(true)
        api.getOrderLog(userId).enqueue(object :
            Callback<ResponseOrderLog> {
            override fun onResponse(call: Call<ResponseOrderLog>, response: Response<ResponseOrderLog>) {
                try {
                    if (response.isSuccessful) {
                        loading(false)
                        val data = response.body()
                        val formatter = DecimalFormat.getCurrencyInstance() as DecimalFormat
                        val symbols = formatter.decimalFormatSymbols
                        symbols.currencySymbol = "Rp. "
                        formatter.decimalFormatSymbols = symbols
                        val totalx = data!!.pendapatan!!.toDouble() ?: 0.0
                        val totals = formatter.format(totalx)
                        val saldox = data.saldo!!.saldo!!.toDouble() ?: 0.0
                        val saldos = formatter.format(saldox)
                        binding.txtSaldo.text = saldos
                        binding.txtPendapatan.text = totals
                        if (response.body()!!.data!!.isEmpty()){
                            binding.txtKosong.visibility = View.VISIBLE
                            binding.rvRiwayat.visibility = View.GONE
                        }else{
                            binding.txtKosong.visibility = View.GONE
                            binding.rvRiwayat.visibility = View.VISIBLE
                            val notesList = mutableListOf<DataLogOrder>()
                            val data = response.body()
                            if (data!!.status == true) {
                                for (hasil in data.data!!) {
                                    notesList.add(hasil!!)
                                }
                                mAdapter = RiwayatOrderAdapter(notesList, requireActivity())
                                binding.rvRiwayat.adapter = mAdapter
                                mAdapter.setDialog(object : RiwayatOrderAdapter.Dialog {
                                    override fun onClick(position: Int, order: OrderLogModel, status: String) {
                                        when (status) {
                                            "0", "1", "2", "3" -> {
                                                val gson = Gson()
                                                val noteJoson = gson.toJson(order)
                                                startActivity<TrackingOrderActivity>("order" to noteJoson)
                                            }
                                            else -> {
//                                                val intent = intentFor<DetailRiwayatOrderActivity>()
//                                                    .putExtra("idOrder", idOrder)
//                                                startActivity(intent)
                                            }
                                        }
                                    }
                                })
                                mAdapter.notifyDataSetChanged()
                            }
                        }
                    } else {
                        loading(false)
                        toast("gagal mendapatkan response")
                    }
                } catch (e: Exception) {
                    if (isAdded) {
                        loading(false)
                        info { "hasan ${e.message}" }
                        toast(e.message.toString())
                    }
                }
            }

            override fun onFailure(call: Call<ResponseOrderLog>, t: Throwable) {
                if (isAdded) {
                    info { "hasan ${t.message}" }
                    toast(t.message.toString())
                }
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

    override fun onStart() {
        super.onStart()
        getData(sessionManager.getId().toString())
    }
}