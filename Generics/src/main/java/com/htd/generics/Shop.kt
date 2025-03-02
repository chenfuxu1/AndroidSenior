package com.htd.generics

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-03-02 16:03
 *
 **/
interface Shop<T> {
    fun buy(): T

    fun refund(item: T)
}