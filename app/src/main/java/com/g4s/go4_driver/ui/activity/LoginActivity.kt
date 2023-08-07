package com.g4s.go4_driver.ui.activity

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.g4s.go4_driver.R
import com.g4s.go4_driver.databinding.ActivityLoginBinding
import com.g4s.go4_driver.model.ResponseLogin
import com.g4s.go4_driver.session.SessionManager
import com.g4s.go4_driver.webservice.ApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity(), AnkoLogger {
    lateinit var binding: ActivityLoginBinding
    var api = ApiClient.instance()
    lateinit var progressDialog: ProgressDialog
    lateinit var sessionManager: SessionManager
    var token : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.lifecycleOwner = this
        progressDialog = ProgressDialog(this)
        sessionManager = SessionManager(this)
        binding.btnlogin.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()){
                login(email,password)
            }else{
                toast("jangan kosongi kolom")
            }
        }
    }

    fun loading(status : Boolean){
        if (status){
            progressDialog.setTitle("Loading...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()
        }else{
            progressDialog.dismiss()
        }
    }

    private fun login(email : String, password : String) {
        loading(true)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                toast("gagal dapat token")
                loading(false)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            token = task.result
            if (token != null) {
                api.login(email,password,token!!).enqueue(object : Callback<ResponseLogin> {
                    override fun onResponse(
                        call: Call<ResponseLogin>,
                        response: Response<ResponseLogin>
                    ) {
                        try {
                            if (response.isSuccessful){
                                if (response.body()!!.status == true) {
                                    sessionManager.setToken(response.body()!!.token!!)
                                    sessionManager.setFcm(token.toString())
                                    sessionManager.setId(response.body()!!.data!!.idUser!!)
                                    sessionManager.setNamaDriver(response.body()!!.data!!.nama!!)
                                    sessionManager.setLogin(true)
                                    loading(false)
                                    toast("login berhasil")
                                    startActivity<MainActivity>()
                                    finish()
                                } else {
                                    loading(false)
                                    toast("Email atau password salah")
                                }

                            }else{
                                loading(false)
                                toast("Kesalahan Jaringan")
                            }
                        }catch (e : Exception){
                            loading(false)
                            info { "hasan ${e.message }${response.code()} " }
                            toast("Kesalahan aplikasi")
                        }
                    }

                    override fun onFailure(call: Call<ResponseLogin>, t: Throwable) {
                        loading(false)
                        info { "erro ${t.message } " }
                        toast("Kesalahan Jaringan")

                    }

                })

            }
        })
    }
    override fun onStart() {
        super.onStart()
        if (sessionManager.getLogin() == true){
            startActivity<MainActivity>()
            finish()
        }
    }

}