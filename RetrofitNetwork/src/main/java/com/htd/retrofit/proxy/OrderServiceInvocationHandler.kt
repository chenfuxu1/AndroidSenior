package com.htd.retrofit.proxy

import com.htd.utils.Sout
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-01-16 23:07
 *
 * Desc:
 */
class OrderServiceInvocationHandler(private val target: Any) : InvocationHandler {
    companion object {
        const val TAG = "OrderServiceInvocationHandler"
    }

    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
        Sout.d(TAG, "日志：准备创建订单... ")
        val result = method.invoke(target, *(args ?: emptyArray()))
        Sout.d(TAG, "日志：订单创建成功！")
        return result
    }
}