package com.cfx.retrofit

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-01-12 23:34
 *
 * Desc:
 */
object GithubRetrofit {
    fun getGithub(): GithubService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        return retrofit.create(GithubService::class.java)
    }
}
