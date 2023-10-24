package com.g4s.go4_driver.webservice

import com.g4s.go4_driver.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("login/driver")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("fcm") fcm: String
    ): Call<ResponseLogin>

    // TODO: ORDER
    @GET("order/driver/{id}")
    fun getOrderLog(
        @Path("id") id: String
    ): Call<ResponseOrderLog>
    @GET("order/detail/{id}")
    fun getDetailOrderLog(
        @Path("id") id: String
    ): Call<ResponseDetailLogOrder>

    //    get cart Count
    @GET("booking/{id}")
    fun getBookingById(
        @Path("id") id: String,
    ): Call<ResponseBooking>

    //TODO: REVIEW
    @GET("review/driver/{id}")
    fun getReview(
        @Path("id") id: String
    ): Call<ResponseUlasan>
}