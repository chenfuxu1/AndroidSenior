package com.htd.threadsync;

import com.htd.utils.Sout;

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-02-06 22:41
 * <p>
 * Desc:
 */
public class SynchronizedDemo1 implements TestDemo {
    // volatile 保证代码同步性
    private volatile boolean mRunning = true;

    private void stop() {
        mRunning = false;
    }

    @Override
    public void runTest() {
        new Thread() {
            @Override
            public void run() {
                while (mRunning) {

                }
            }
        }.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Sout.INSTANCE.d(e);
        }
        stop();
    }
}
