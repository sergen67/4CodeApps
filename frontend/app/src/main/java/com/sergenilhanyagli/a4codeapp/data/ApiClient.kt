package com.sergenilhanyagli.a4codeapp.data

import com.sergenilhanyagli.a4codeapp.data.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.getValue

object ApiClient {
    private const val BASE_URL = "https://fourcodeapps.onrender.com/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
