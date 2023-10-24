package com.g4s.go4_driver.ui.activity

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.g4s.go4_driver.R
import com.g4s.go4_driver.databinding.ActivityProfileBinding
import com.g4s.go4_driver.session.SessionManager
import com.g4s.go4_driver.webservice.ApiClient
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class ProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileBinding
    lateinit var sessionManager: SessionManager
    var api = ApiClient.instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        binding.lifecycleOwner = this
        sessionManager = SessionManager(this)
        binding.txtDriver.text = sessionManager.getNamaDriver().toString().uppercase()

        binding.ulasan.setOnClickListener {
            startActivity<UlasanActivity>()
        }
        binding.logout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Logout ? ")
            builder.setPositiveButton("Ok") { dialog, which ->
                sessionManager.clearSession()
                startActivity<LoginActivity>()
                toast("Berhasil Logout")
                this.finish()
            }

            builder.setNegativeButton("Cancel ?") { dialog, which ->
                dialog.dismiss()
            }

            builder.show()
        }
    }
}