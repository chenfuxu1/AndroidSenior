package com.htd.threadsync;



import com.htd.utils.Sout;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-02-01 22:15
 **/
public class Main {
    private static final String TAG = "Main";

    /**
     * thread
     */
    static void thread() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Sout.INSTANCE.d(TAG, "cfx Thread started");
            }
        };
        thread.start();
    }

    /**
     * runnable
     */
    static void runnable() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Sout.INSTANCE.d(TAG, "cfx Thread with runnable started!");
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * thread factory
     */
    static void threadFactory() {
        ThreadFactory factory = new ThreadFactory() {
            private AtomicInteger mCount = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Thread - " + mCount.incrementAndGet());
            }
        };

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Sout.INSTANCE.d(TAG, "cfx " + Thread.currentThread().getName() + " started!");
            }
        };

        Thread thread = factory.newThread(runnable);
        thread.start();

        Thread thread1 = factory.newThread(runnable);
        thread1.start();
    }

    /**
     * executor
     */
    static void executor() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Sout.INSTANCE.d(TAG, "cfx executor thread started!");
            }
        };

        /**
         *  public static ExecutorService newCachedThreadPool() {
         *      return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
         *                                    60L, TimeUnit.SECONDS,
         *                                    new SynchronousQueue<Runnable>());
         *  }
         *  参数 1：默认核心线程数，当线程不再使用时，会回收，设置 0 就回收所有
         *  参数 2：线程上限数
         *  参数 3、4：等待闲置线程回收的时间
         *  参数 5：SynchronousQueue 队列的主要作用是直接在线程之间传递任务，减少线程创建和销毁的开销‌。
         *  SynchronousQueue 是一个特殊的阻塞队列，其长度为 0，这意味着它不存储任何元素。
         *  当一个线程往 SynchronousQueue 中放入一个元素时，它会阻塞等待另一个线程从队列中取出该元素。
         *  同样，当一个线程从队列中取出元素时，也会阻塞等待另一个线程放入元素‌
         */
        Executor executor = Executors.newCachedThreadPool();
        executor.execute(runnable);
        executor.execute(runnable);
        executor.execute(runnable);

        /**
         * public static ExecutorService newSingleThreadExecutor() {
         *     return new FinalizableDelegatedExecutorService
         *         (new ThreadPoolExecutor(1, 1,
         *                                 0L, TimeUnit.MILLISECONDS,
         *                                 new LinkedBlockingQueue<Runnable>()));
         * }
         * 单线程的线程池
         * 核心线程和线程上限都是 1，也没有回收的时间
         * 应用场景较少
         * LinkedBlockingQueue：大小不定的 BlockingQueue, 若其构造函数带一个规定大小的参数, 生成的 BlockingQueue 有大小限制,
         * 若不带大小参数, 所生成的 BlockingQueue 的大小由Integer.MAX_VALUE 来决定.其所含的对象是以
         * FIFO( 先入先出 )顺序排序的
         */
        Executors.newSingleThreadExecutor();

        /**
         * public static ExecutorService newFixedThreadPool(int nThreads) {
         *     return new ThreadPoolExecutor(nThreads, nThreads,
         *                                   0L, TimeUnit.MILLISECONDS,
         *                                   new LinkedBlockingQueue<Runnable>());
         * }
         * 指定线程池大小
         * 核心线程和上线都是固定的，不会回收线程
         * 所以，不能修改线程上限，应用场景较少，适用于处理集中爆发的任务
         */
        Executors.newFixedThreadPool(5);

        // 可以延时的线程池
        Executors.newScheduledThreadPool(5);
    }

    /**
     * 有返回值的 runnable
     */
    static void callable() {
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Sout.INSTANCE.d(TAG, e.toString());
                }
                return "Done";
            }
        };
        ExecutorService executorService = Executors.newCachedThreadPool();
        Future<String> future = executorService.submit(callable);
        try {
            // 阻塞式的拿结果
            String result = future.get();
            Sout.INSTANCE.d(TAG, "cfx result: " + result);
        } catch (Exception e) {
            Sout.INSTANCE.d(TAG, e.toString());
        }

        // 正确使用是放在循环中
    }

    static void runSynchronizedDemo1() {
        new SynchronizedDemo1().runTest();
    }

    static void runSynchronizedDemo2() {
        new SynchronizedDemo2().runTest();
    }

    static void runSynchronizedDemo3() {
        new SynchronizedDemo3().runTest();
    }
}