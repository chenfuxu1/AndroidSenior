package com.htd.threadsync;

import com.htd.utils.Sout;

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-02-06 22:41
 * <p>
 * Desc:
 */
public class SynchronizedDemo2 implements TestDemo {
    // volatile 保证代码同步性, 虽然同步，但是 mX++ 不是原子操作，所以结果还是不对
    private volatile int mX =0;

    // 加锁保证同步性，同一时刻只有一个线程可以访问该方法
    private synchronized void count() {
        mX++;
    }

    @Override
    public void runTest() {
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 1_000_000; i++) {
                    count();
                }
                Sout.INSTANCE.d("cfx 1 mX: " + mX);
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 1_000_000; i++) {
                    count();
                }
                Sout.INSTANCE.d("cfx 2 mX: " + mX);
            }
        }.start();
    }
}
