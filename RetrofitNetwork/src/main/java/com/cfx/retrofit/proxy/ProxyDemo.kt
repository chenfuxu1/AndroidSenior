package com.cfx.retrofit.proxy

import java.lang.reflect.Proxy

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-01-16 23:08
 *
 * Desc:
 */
fun main() {
    val orderService = OrderServiceImpl()
    val proxy = Proxy.newProxyInstance(
        orderService::class.java.classLoader,
        orderService::class.java.interfaces,
        OrderServiceInvocationHandler(orderService)
    ) as OrderService
    proxy.createOrder()
}