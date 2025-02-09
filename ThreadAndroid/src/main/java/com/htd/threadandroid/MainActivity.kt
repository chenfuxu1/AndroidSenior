package com.htd.threadandroid

import android.os.Bundle
import android.os.Handler
import android.os.Handler.Callback
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/**
 * handler 的目标
 * 可以在指定的线程中运行代码
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // val handler = Handler(object : Callback {
        //     override fun handleMessage(msg: Message): Boolean {
        //         when (msg.arg1) {
        //             //
        //         }
        //     }
        //
        // })
        Thread {
            // handler.post {
            //     // 将任务 post 到主线程执行
            // }
        }.start()

        Looper.myLooper() // 获取当前线程的 looper，存在每个线程的 ThreadLocal 中
        Looper.getMainLooper() // 获取主线程的 looper

        // 可以给一个 Looper 创建多个 handler
        val handlerThread = HandlerThread("second")
        val handler1 = Handler(Looper.getMainLooper())
        val handler2 = Handler(handlerThread.looper)

    }
}

/**
 * HandlerThread
 * @Override
 * public void run() {
 *     mTid = Process.myTid();
 *     Looper.prepare();
 *     synchronized (this) {
 *         // 当前线程的 looper
 *         mLooper = Looper.myLooper();
 *         notifyAll();
 *     }
 *     Process.setThreadPriority(mPriority);
 *     onLooperPrepared();
 *     Looper.loop();
 *     mTid = -1;
 * }
 *
 * Looper#prepare
 * public static void prepare() {
 *     prepare(true);
 * }
 *
 * Looper#
 * private static void prepare(boolean quitAllowed) {
 *     if (sThreadLocal.get() != null) {
 *         throw new RuntimeException("Only one Looper may be created per thread");
 *     }
 *     sThreadLocal.set(new Looper(quitAllowed)); // 可以发现，每个线程会创建一个 looper 放到 sThreadLocal 中
 * }
 *
 * Looper#loop()
 * public static void loop() {
 *     final Looper me = myLooper();
 *     if (me == null) {
 *         throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
 *     }
 *     if (me.mInLoop) {
 *         Slog.w(TAG, "Loop again would have the queued messages be executed"
 *                 + " before this one completed.");
 *     }
 *
 *     me.mInLoop = true;
 *
 *     // Make sure the identity of this thread is that of the local process,
 *     // and keep track of what that identity token actually is.
 *     Binder.clearCallingIdentity();
 *     final long ident = Binder.clearCallingIdentity();
 *
 *     // Allow overriding a threshold with a system prop. e.g.
 *     // adb shell 'setprop log.looper.1000.main.slow 1 && stop && start'
 *     final int thresholdOverride =
 *             SystemProperties.getInt("log.looper."
 *                     + Process.myUid() + "."
 *                     + Thread.currentThread().getName()
 *                     + ".slow", -1);
 *
 *     me.mSlowDeliveryDetected = false;
 *
 *     // 死循环
 *     for (;;) {
 *         if (!loopOnce(me, ident, thresholdOverride)) {
 *             return;
 *         }
 *     }
 * }
 */