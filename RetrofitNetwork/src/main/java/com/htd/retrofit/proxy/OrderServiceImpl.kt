package com.htd.retrofit.proxy

import com.htd.utils.Sout


/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-01-16 23:06
 *
 * Desc:
 */
class OrderServiceImpl : OrderService {
    companion object {
        const val TAG = "OrderServiceImpl"
    }

    override fun createOrder() {
        Sout.d(TAG, " 订单已创建！")
    }
}