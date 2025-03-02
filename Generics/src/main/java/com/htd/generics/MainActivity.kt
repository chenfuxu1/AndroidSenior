package com.htd.generics

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.htd.generics.bean.Apple
import com.htd.generics.bean.Banana
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
}