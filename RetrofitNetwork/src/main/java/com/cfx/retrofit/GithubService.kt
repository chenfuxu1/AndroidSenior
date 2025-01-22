package com.cfx.retrofit

import io.reactivex.Observable
import retrofit2.Call
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
    fun listRepos(@Path("user") user: String): Call<List<Repo>>

    @GET("users/{user}/repos")
    fun listReposRx(@Path("user") user: String): Observable<List<Repo>>
}