package com.htd.rxjava.network

import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Retrofit
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
            // .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io())) // 所有的请求都在后台
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
        return retrofit.create(GithubService::class.java)
    }
}
