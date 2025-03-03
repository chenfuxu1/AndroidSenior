package com.htd.generics

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.htd.generics.fruit.Apple
import com.htd.generics.fruit.Banana
import com.htd.generics.fruit.Fruit
import com.htd.generics.sim.ChinaMobile
import com.htd.utils.Logit

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val wrapper = Wrapper<String>()
        wrapper.instance = "荒天帝"
        Logit.d(TAG, "cfx " + wrapper.instance.toString())

        val wrapperList = WrapperList<String>()
        wrapperList.add("张三")
        val name = wrapperList.get(0)
        Logit.d(TAG, "cfx name: $name")

        val repaireShop = RepaireShop<Banana>()
        Logit.d(TAG, "cfx repaireShop: ${repaireShop::class.java}")

        val simShop: SimShop<Apple, ChinaMobile>
        // val bananaShop: SimShop<Apple, Banana> // Type argument is not within its bounds
    }

    fun test() {
        // 1.苹果的集合
        val apples: ArrayList<Apple> = ArrayList<Apple>()
        val banana = Banana("香蕉")
        // apples.add(banana) // apples 的 list 不能添加香蕉

        // 2.水果的集合
        val fruits: ArrayList<Fruit> = ArrayList<Fruit>()
        fruits.add(banana)
        fruits.add(Apple("苹果"))

        // 3.fruitList: ArrayList<Fruit> = ArrayList<Apple>(), 如果不报错，会导致 苹果列表中会添加香蕉
        // val appleList: ArrayList<Apple> = ArrayList<Apple>()
        // val fruitList: ArrayList<Fruit> = appleList
        // fruitList.add(banana)

        // 不能添加元素，只能获取
        val appleList3: ArrayList<Apple> = ArrayList<Apple>()
        val fruitList3: ArrayList<*> = appleList3
        // fruitList3.add(banana)
        // fruitList3.add(Apple(""))
        // fruitList3.add(Fruit(""))

        // * 是上届通配符， java 中是 ？
        // 需要的是水果店，返回的是苹果店, 加完通配符后，只能 get，不能 set，任何类型都不能 set
        // 加完 * 通配符后，所有带类型（输入）的方法都被禁用
        val shop: Shop<*> = object : Shop<Apple> {
            override fun buy(): Apple {
                TODO("Not yet implemented")
            }

            override fun refund(item: Apple) {
                TODO("Not yet implemented")
            }

        }
        // shop.refund(Apple(""))
        // shop.refund(Banana(""))
        // shop.refund(Fruit(""))

        // val fruit4: Array<Fruit> = arrayOf<Apple>()
        // fruit4[0] = Banana("香蕉")
    }
}