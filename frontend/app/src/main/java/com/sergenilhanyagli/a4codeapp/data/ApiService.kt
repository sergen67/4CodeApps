package com.sergenilhanyagli.a4codeapp.data

import com.sergenilhanyagli.a4codeapp.data.models.User
import com.sergenilhanyagli.a4codeapp.data.models.LoginRequest
import com.sergenilhanyagli.a4codeapp.data.models.Product
import com.sergenilhanyagli.a4codeapp.data.models.Order
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("register")
    suspend fun register(@Body user: User): Response<User>

    @POST("login")
    suspend fun login(@Body creds: LoginRequest): Response<User>

    @GET("products")
    suspend fun getProducts(): Response<List<Product>>

    @POST("orders")
    suspend fun createOrder(@Body order: Order): Response<Order>

    @GET("orders")
    suspend fun getOrders(): Response<List<Order>>

    // ✅ Ürün ekleme
    @POST("products")
    suspend fun createProduct(@Body product: HashMap<String, Any>): Response<Product>

    @PUT("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int?,
        @Body product: HashMap<String, Any>
    ): Response<Product>

    @DELETE("products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: Int?,
        @Query("role") role: String = "admin"
    ): Response<Map<String, String>>
    @GET("users")
    suspend fun getUsers(): Response<List<User>>

    @POST("register")
    suspend fun register(@Body user: HashMap<String, Any>): Response<User>
    @POST("sales")
    suspend fun createSale(@Body body: HashMap<String, Any>): Response<Map<String, Any>>


    @GET("sales")
    suspend fun getSales(): Response<List<Map<String, Any>>>

    @GET("sales/daily")
    suspend fun getDailyRevenue(): Response<List<Map<String, Any>>>

    @GET("sales/monthly")
    suspend fun getMonthlyRevenue(): Response<List<Map<String, Any>>>
    @GET("categories")
    suspend fun getCategories(): Response<List<Map<String, Any>>>

    @POST("categories")
    suspend fun createCategory(@Body body: HashMap<String, Any>): Response<Map<String, Any>>
    @GET("sales/daily")
    suspend fun getSalesDaily(): Response<List<Map<String, Any>>>

    @GET("sales/monthly")
    suspend fun getSalesMonthly(): Response<List<Map<String, Any>>>

    @GET("/sales/weekly")
    suspend fun getSalesWeekly(): Response<List<Map<String, Any>>>

}
