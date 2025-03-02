package com.htd.generics

import com.htd.generics.sim.Sim

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-03-02 16:40
 *
 **/
interface SimShop<T, C : Sim> : Shop<T> {
    fun getSim(name : String, id : String): C
}