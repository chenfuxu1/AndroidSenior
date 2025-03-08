package com.htd.generics

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-03-02 16:03
 *
 **/
interface Shop<T, E> {
    fun buy(): T

    fun refund(item: T)

    // 回收，不限制回收任何东西, 如果想要接收任意类型就不能用泛型，只能换成 Object
    // fun recycle(item: Any)
    fun recycle(item: E): MutableList<T>
}

interface Shop2<T> {
    fun buy(): T

    fun refund(item: T)

    // 回收，不限制回收任何东西, 如果想要接收任意类型就不能用泛型，只能换成 Object
    // fun recycle(item: Any)
    // fun recycle(item: E): MutableList<T>

    // 旧换新，输入和输出是同一种类型，在方法上声明
    fun <E> tradeIn(item: E, money: Float) : E
}