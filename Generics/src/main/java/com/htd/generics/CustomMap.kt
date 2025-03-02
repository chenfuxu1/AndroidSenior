package com.htd.generics

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-03-02 16:33
 *
 **/
class CustomMap<K, V> {
    fun put(key: K, value: V) {}

    fun get(key: K): V? {
        return Any() as V
    }
}