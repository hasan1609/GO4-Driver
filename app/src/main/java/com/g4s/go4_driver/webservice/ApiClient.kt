package com.g4s.go4_driver.webservice

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient {
    companion object{
        private var retrofit : Retrofit? = null
        private var opt = OkHttpClient.Builder().apply {
            connectTimeout(5, TimeUnit.MINUTES)
            readTimeout(5, TimeUnit.MINUTES)
            writeTimeout(5, TimeUnit.MINUTES)
        }.build()

        private fun getClient() : Retrofit {
            return if (retrofit == null){
                retrofit = Retrofit.Builder().apply {
                    client(opt)
                    baseUrl("http://192.168.2.14/go4-sumbergedang/rest-g4s/public/api/")
                    addConverterFactory(GsonConverterFactory.create())
                }.build()
                retrofit!!
            }else{
                retrofit!!
            }
        }

        fun instance(): ApiService = getClient().create(ApiService::class.java)
    }
}