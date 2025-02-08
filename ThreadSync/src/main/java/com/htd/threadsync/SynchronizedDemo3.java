package com.htd.threadsync;

import com.htd.utils.Sout;

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-02-06 22:41
 * <p>
 * Desc:
 */
public class SynchronizedDemo3 implements TestDemo {
    private int mX = 0;
    private int mY = 0;
    private String mName;

    private synchronized void count(int newValue) {
        mX = newValue;
        mY = newValue;
    }

    private void minus(int delta) {
        synchronized (this) {
            mX -= delta;
            mY -= delta;
        }
    }

    private void setName(String name) {
        mName = name;
    }

    @Override
    public void runTest() {

    }
}
