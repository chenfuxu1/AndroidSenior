package com.htd.rxjava.network

import com.htd.rxjava.bean.Repo
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-01-12 22:53
 *
 * Desc:
 */
interface GithubService {
    @GET("users/{user}/repos")
    fun listReposRx(@Path("user") user: String): Single<MutableList<Repo>> // Single 是单一事件，Observable 是多事件
}