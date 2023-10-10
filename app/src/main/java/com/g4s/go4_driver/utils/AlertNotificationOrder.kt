package com.g4s.go4_driver.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import com.g4s.go4_driver.R

class AlertNotificationOrder private constructor(context: Context) {
    private var dialog: Dialog? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_alert_recive_booking, null)
        dialog = Dialog(context)
        dialog?.setContentView(view)
        dialog?.setCancelable(false)

        val btnTerima = view.findViewById<Button>(R.id.btn_terima)
        val btnTolak = view.findViewById<Button>(R.id.btn_tolak)
        btnTerima.setOnClickListener {
            dismiss()
        }
    }

    fun show() {
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
    }


}