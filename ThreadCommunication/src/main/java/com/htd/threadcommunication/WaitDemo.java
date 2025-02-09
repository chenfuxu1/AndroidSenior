package com.htd.threadcommunication;

import com.htd.utils.Sout;

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-02-09 11:00
 * <p>
 * Desc:
 */
public class WaitDemo implements TestDemo {
    private static final String TAG = "WaitDemo";
    private String mSharedString;

    private synchronized void initString() {
        mSharedString = "荒天帝";
        notifyAll();
    }

    private synchronized void printString() {
        while (mSharedString == null) {
            try {
                wait();
            } catch (InterruptedException e) {

            }
        }
        Sout.INSTANCE.d(TAG, mSharedString);
    }

    @Override
    public void runTest() {
        Thread thread1 = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Sout.INSTANCE.d(TAG, e);
                }
                initString();
            }
        };
        thread1.start();

        Thread thread2 = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Sout.INSTANCE.d(TAG, e);
                }
                printString();
            }
        };
        thread2.start();

        try {
            // 线程 2 执行完毕才会往下走
            thread2.join();
        } catch (InterruptedException e) {

        }
        Sout.INSTANCE.d(TAG, "cfx finished");
    }
}
