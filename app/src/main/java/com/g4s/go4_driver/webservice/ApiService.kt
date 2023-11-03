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
    // update password
    @POST("update-password")
    fun updatePassword(
        @Field("id") id: String,
        @Field("current_password") current_password: String,
        @Field("password") password: String,
    ): Call<ResponsePostData>

    // TODO: ORDER
    // get order log
    @GET("order/driver/{id}")
    fun getOrderLog(
        @Path("id") id: String
    ): Call<ResponseOrderLog>
    // get detail order log
    @GET("order/detail/{id}")
    fun getDetailOrderLog(
        @Path("id") id: String
    ): Call<ResponseDetailLogOrder>
    // update status
    @FormUrlEncoded
    @POST("order/status")
    fun updateStatusOrder(
        @Field("id") id: String,
        @Field("status") status: String,
    ): Call<ResponsePostData>
    // cek booking
    @GET("order/cek/{id}")
    fun cekBooking(
        @Path("id") id: String
    ): Call<ResponseCekBooking>

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