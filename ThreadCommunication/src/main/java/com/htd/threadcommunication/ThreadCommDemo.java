package com.htd.threadcommunication;

import com.htd.utils.Sout;

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-02-08 23:54
 * <p>
 * Desc:
 */
public class ThreadCommDemo implements TestDemo {
    private static final String TAG = "ThreadCommDemo";

    @Override
    public void runTest() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 1_000_000; i++) {
                    // if (isInterrupted()) {
                    //     return;
                    // }
                    Sout.INSTANCE.d(TAG, "cfx i = " + i);
                }
            }
        };
        thread.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Sout.INSTANCE.d(TAG, "cfx e: " + e);
        }
        // stop 方法的结果不可预期，可能使程序处于中间状态，所以弃用了
        thread.stop();

        // thread.interrupt(); // 是温和型，不是强制的
    }
}
