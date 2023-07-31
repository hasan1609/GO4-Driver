package com.g4s.go4_driver.webservice

import com.g4s.go4_driver.model.ResponseLogin
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    @FormUrlEncoded
    @POST("login/driver")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("fcm") fcm: String
    ): Call<ResponseLogin>
}