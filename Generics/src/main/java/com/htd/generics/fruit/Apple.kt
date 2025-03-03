package com.htd.generics.fruit

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-03-02 16:09
 *
 **/
data class Apple(val name: String) : Fruit {
    // in Apple 扩大传入的范围
    // 这里的传入的集合不仅仅希望是 List<Apple>, 也希望传入 List<Fruit>
    // in / ? super 只关心设置，不关心获取
    // out / ? extends 只关心获取，不关心设置
    fun addMeToList(list: MutableList<in Apple>) {
        list.add(this)
    }
}
