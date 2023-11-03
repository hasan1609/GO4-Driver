package com.g4s.go4_driver.ui.activity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import com.g4s.go4_driver.R
import com.g4s.go4_driver.databinding.ActivityUpdatePasswordBinding
import com.g4s.go4_driver.model.ResponsePostData
import com.g4s.go4_driver.session.SessionManager
import com.g4s.go4_driver.webservice.ApiClient
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdatePasswordActivity : AppCompatActivity(), AnkoLogger {
    private lateinit var binding: ActivityUpdatePasswordBinding
    lateinit var sessionManager: SessionManager
    private lateinit var progressDialog: ProgressDialog
    private var api = ApiClient.instance()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_password)
        binding.lifecycleOwner = this
        sessionManager = SessionManager(this)
        progressDialog = ProgressDialog(this)
        setupUi()
    }

    private fun setupUi() {
        binding.appBar.backButton.setOnClickListener {
            onBackPressed()
        }
        binding.appBar.titleTextView.text = "Update Password"
        binding.edtCurrentPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_eye_show, 0)
        // Atur tindakan saat gambar mata terbuka diklik
        binding.edtCurrentPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (event.rawX >= (binding.edtCurrentPassword.right - binding.edtCurrentPassword.compoundDrawables[2].bounds.width())) {
                    // Gambar mata terbuka diklik
                    togglePasswordVisibility(binding.edtCurrentPassword)
                    return@setOnTouchListener true
                }
            }
            false
        }
        binding.edtNewPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (event.rawX >= (binding.edtNewPassword.right - binding.edtNewPassword.compoundDrawables[2].bounds.width())) {
                    // Gambar mata terbuka diklik
                    togglePasswordVisibility(binding.edtNewPassword)
                    return@setOnTouchListener true
                }
            }
            false
        }
        binding.edtCekPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (event.rawX >= (binding.edtCekPassword.right - binding.edtCekPassword.compoundDrawables[2].bounds.width())) {
                    // Gambar mata terbuka diklik
                    togglePasswordVisibility(binding.edtCekPassword)
                    return@setOnTouchListener true
                }
            }
            false
        }

        binding.btnSimpan.setOnClickListener {
            val password_old = binding.edtCurrentPassword.text.toString()
            val password_new = binding.edtNewPassword.text.toString()
            val password_cek = binding.edtCekPassword.text.toString()
            val regex = Regex("^(?=.*\\d).{8,}\$")
            if (password_old.isNotEmpty() && password_new.isNotEmpty() && password_cek.isNotEmpty()){
                if (password_new == password_old){
                    toast("Password Baru Tidak Boleh Sama")
                }else{
                    if (password_new == password_cek){
                        if (password_new.matches(regex)){
                            updatePassword(
                                sessionManager.getId().toString(),
                                password_old,
                                password_new
                            )
                        }else{
                            toast("Password Harus Mengandung Huruf Besar dan Angka")
                        }
                    }else{
                        toast("Pasword tidak sama")
                    }
                }
            }else{
                toast("Harap Isi Semua Data")
            }
        }
    }

    private fun updatePassword(id: String, oldPassword: String, newPassword: String) {
        loading(true)
        api.updatePassword(
            id,
            oldPassword,
            newPassword
        ).enqueue(object : Callback<ResponsePostData> {
            override fun onResponse(
                call: Call<ResponsePostData>,
                response: Response<ResponsePostData>
            ) {
                if (response.isSuccessful) {
                    loading(false)
                    if (response.body()!!.status == true){
                        toast("Password Berhasil diubah")
                        finish()
                    }else{
                        toast("Password Salah")
                        info(response.body()!!.message)
                    }
                } else {
                    loading(false)
                    toast(response.body().toString())
                    info(response)
                }
            }

            override fun onFailure(call: Call<ResponsePostData>, t: Throwable) {
                loading(false)
                toast("Terjadi kesalahan")
            }
        })
    }

    private fun togglePasswordVisibility(editText: EditText) {
        if (editText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_eye_hide, 0)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_eye_show, 0)
        }
        // Set kursor ke akhir teks
        editText.setSelection(editText.text.length)
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
}