package com.htd.generics

import com.htd.generics.bean.Apple

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-03-02 16:05
 *
 **/
class RealShop : Shop<Apple> {
    override fun buy(): Apple {
        TODO("Not yet implemented")
    }

    override fun refund(item: Apple) {
        TODO("Not yet implemented")
    }
}