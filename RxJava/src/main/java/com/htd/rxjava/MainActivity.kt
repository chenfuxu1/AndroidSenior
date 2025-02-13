package com.htd.rxjava

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.htd.rxjava.bean.Repo
import com.htd.rxjava.network.GithubRetrofit
import com.htd.utils.Logit
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * RxJava 基本使用
 */
class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }
    lateinit var rxTv: TextView
    var disposable : Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        initView()
        networkRequest()
    }

    private fun initView() {
        rxTv = findViewById(R.id.rx_tv)
    }

    private fun networkRequest() {
        val retrofit = GithubRetrofit.getGithub()
        retrofit.listReposRx("chenfuxu1")
            .subscribeOn(Schedulers.io()) // io 线程请求，也可以在接口 addCallAdapterFactory 中设置
            .observeOn(AndroidSchedulers.mainThread()) // 数据回来到主线程
            .subscribe(object : SingleObserver<MutableList<Repo>> {
                // 订阅
                override fun onSubscribe(d: Disposable) {
                    rxTv.text = "正在请求..."
                    disposable = d
                }

                // 数据返回
                override fun onSuccess(repos: MutableList<Repo>) {
                    rxTv.text = "Result: ${repos[0].name}"
                }

                override fun onError(e: Throwable) {
                    Logit.d(TAG, e)
                    rxTv.text = e.message ?: e.javaClass.name
                }

            })
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }
}
