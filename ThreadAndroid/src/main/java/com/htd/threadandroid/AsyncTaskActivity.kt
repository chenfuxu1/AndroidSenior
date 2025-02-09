package com.htd.threadandroid

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.htd.utils.Sout
import java.lang.ref.WeakReference

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-02-09 17:16
 *
 * Desc: AsyncTask 的内存泄露
 */
class AsyncTaskActivity : AppCompatActivity() {
    companion object {
        var name = "荒天帝"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val myAsyncTask = MyAsyncTask()
        myAsyncTask.execute()
    }

    class MyAsyncTask : AsyncTask<Unit, Int, Boolean>() {
        override fun onPreExecute() {
            super.onPreExecute()
            Sout.d(name) // 内部类持有外部类的引用
        }
        override fun doInBackground(vararg params: Unit?): Boolean {
            TODO("Not yet implemented")
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
        }
    }

    /**
     * 说法：当界面关闭时，因为内部类持有外部类的引用，导致内存泄漏
     * 那为什么下面的方式不会导致内存泄露呢
     */
    class MyView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {
        var viewName = name // 难道自定义 view 都要强制静态内部类吗
    }

    // 弱引用
    class User {
        val activityWeakRef = WeakReference(AsyncTaskActivity)
        val userName = activityWeakRef.get()?.name
    }

    /**
     * 但是在实际场景中，自定义 view 和 弱引用 都不会导致内存泄露，这是为什么呢
     *
     * 垃圾回收机制：
     * 没有被 GC Root 直接或间接引用的对象才会被回收
     *
     * 下面场景不会回收：
     * 1.正在运行的线程不会回收
     * 2.静态代码
     * 3.本地对象指过来的引用
     *
     * 所以，asyncTask 是由于界面关闭时，还有线程在运行才会导致内存泄露，如果没有线程运行就不会导致内存泄露
     * 因为 asyncTask 的后台任务一般很快就会结束了，虽然当前没被回收，但下次就会被回收，这种情况不需要额外特殊处理
     * 如果特别耗时就不能用 asyncTask, 而是应该用 services
     */
}

/**
 * Thread 不用，太难管理
 * Executor 能用则用，多线程
 * AsyncTask 如果场景合适可以用，没什么缺陷
 * HandlerThread 单线程，不适合大批量的线程
 * IntentService 是个 service，比较重，一般不用
 */


