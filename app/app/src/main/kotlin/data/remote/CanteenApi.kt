package com.example.appcantina.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CanteenApi {
    @GET("menu/today")
    suspend fun todayMenu(): RemoteMenuResponse

    @POST("orders")
    suspend fun sendOrder(@Body request: RemoteOrderRequest): RemoteOrderResponse

    @GET("orders/{id}/status")
    suspend fun orderStatus(@Path("id") orderId: Long): RemoteOrderStatusResponse

    @GET("forms/responses")
    suspend fun formResponses(): List<RemoteFormResponse>
}
