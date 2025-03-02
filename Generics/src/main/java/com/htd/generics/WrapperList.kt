package com.htd.generics

import java.util.Arrays

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-03-02 11:19
 *
 **/
class WrapperList<T> {
    private var instances = arrayOf<Any?>()

    fun get(index: Int): T {
        return instances[index] as T
    }

    fun set(index: Int, newObj: T) {
        instances[index] = newObj
    }

    fun add(newObj: T) {
        instances = Arrays.copyOf(instances, instances.size + 1)
        instances[instances.size - 1] = newObj
    }
}