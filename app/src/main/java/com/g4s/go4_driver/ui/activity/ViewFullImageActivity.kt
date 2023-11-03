package com.g4s.go4_driver.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.g4s.go4_driver.R
import kotlinx.android.synthetic.main.activity_view_full_image.*

class ViewFullImageActivity : AppCompatActivity() {

    companion object {
        const val IMAGE_URL = "IMAGE_URL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_full_image)

        val imageUrl = intent.getStringExtra(IMAGE_URL)
        Glide.with(this).load(imageUrl).into(img_viewer)
    }
}