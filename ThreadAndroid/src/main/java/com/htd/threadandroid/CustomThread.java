package com.htd.threadandroid;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-02-09 16:21
 * <p>
 * Desc:
 */
public class CustomThread extends Thread {
    private Looper mLooper = new Looper();

    @Override
    public void run() {
        mLooper.loop();
    }

    class Looper {
        private Runnable mTask;
        private final AtomicBoolean mQuit = new AtomicBoolean(false);

        public synchronized void setTask(Runnable task) {
            mTask = task;
        }

        public void quit() {
            mQuit.set(true);
        }

        void loop() {
            while (!mQuit.get()) {
                synchronized (this) {
                    if (mTask != null) {
                        mTask.run();
                        mTask = null;
                    }
                }

            }
        }
    }
}
