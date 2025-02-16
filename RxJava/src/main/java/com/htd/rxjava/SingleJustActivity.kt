package com.htd.rxjava

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.htd.utils.Logit
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-02-09 23:15
 *
 * Desc:
 */
class SingleJustActivity : AppCompatActivity() {
    companion object {
        const val TAG = "SingleJustActivity"
    }

    lateinit var rxTv: TextView
    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_single_just)
        initView()

        // singleJust()
        // singleMap()
        // cancelObservable()
        obervableMap()
    }

    private fun initView() {
        rxTv = findViewById(R.id.rx_tv)
    }

    /**
     * 单事件的，未发起网络请求
     *
     * Single#just
     * public static <@NonNull T> Single<T> just(T item) {
     *     // 1.判空操作
     *     Objects.requireNonNull(item, "item is null");
     *     // 2.主要是一个钩子函数
     *     return RxJavaPlugins.onAssembly(new SingleJust<>(item));
     * }
     * Single#onAssembly
     * 钩子函数，雁过插毛器，传入 source，又返回了 source
     * public static <T> Single<T> onAssembly(@NonNull Single<T> source) {
     *     Function<? super Single, ? extends Single> f = onSingleAssembly;
     *     if (f != null) {
     *         return apply(f, source);
     *     }
     *     return source;
     * }
     * 所以这里主要看下 source 也就是 new SingleJust<>(item)
     * SingleJust
     * public final class SingleJust<T> extends Single<T> {
     *     final T value;
     *     public SingleJust(T value) {
     *         this.value = value;
     *     }
     *     @Override
     *     protected void subscribeActual(SingleObserver<? super T> observer) {
     *         observer.onSubscribe(Disposable.disposed());
     *         observer.onSuccess(value);
     *     }
     * }
     * 这里我们可以先看下上文 single.subscribe 方法
     * Single#subscribe
     * public final void subscribe(@NonNull SingleObserver<? super T> observer) {
     *     // 1.判空
     *     Objects.requireNonNull(observer, "observer is null");
     *     // 2.钩子
     *     observer = RxJavaPlugins.onSubscribe(this, observer);
     *     // 3.判空
     *     Objects.requireNonNull(observer, "The RxJavaPlugins.onSubscribe hook returned a null SingleObserver. Please check the handler provided to RxJavaPlugins.setOnSingleSubscribe for invalid null returns. Further reading: https://github.com/ReactiveX/RxJava/wiki/Plugins");
     *     try {
     *         // 所以主要的方法就是这一行
     *         subscribeActual(observer);
     *     } catch (NullPointerException ex) {
     *         throw ex;
     *     } catch (Throwable ex) {
     *         Exceptions.throwIfFatal(ex);
     *         NullPointerException npe = new NullPointerException("subscribeActual failed");
     *         npe.initCause(ex);
     *         throw npe;
     *     }
     * }
     * 因此主要方法就是 subscribeActual(observer)，这是一个抽象方法，主要由 Single 的各个子类实现，这里由 SingleJust 实现
     * 所以，主要方法就两行：
     * observer.onSubscribe(Disposable.disposed());
     * observer.onSuccess(value);
     * 在 SingleJust 中，订阅后就完成了就会调 observer.onSuccess 回调相应的 value
     * 为什么没有调 onError 呢，因为这种场景不会出错
     */
    private fun singleJust() {
        val single = Single.just("荒天帝")
        single.subscribe(object : SingleObserver<String> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onError(e: Throwable) {

            }

            override fun onSuccess(t: String) {
                rxTv.text = t
            }

        })
    }

    /**
     * 操作符
     *
     * public final <@NonNull R> Single<R> map(@NonNull Function<? super T, ? extends R> mapper) {
     *     Objects.requireNonNull(mapper, "mapper is null");
     *     // this: 上游 single 对象 mapper: 监听的对象（object: Function<Int, String>{}）
     *     return RxJavaPlugins.onAssembly(new SingleMap<>(this, mapper));
     * }
     *
     * public final class SingleMap<T, R> extends Single<R> {
     *     final SingleSource<? extends T> source; // single 对象
     *
     *     final Function<? super T, ? extends R> mapper; // object: Function<Int, String>{}
     *
     *     public SingleMap(SingleSource<? extends T> source, Function<? super T, ? extends R> mapper) {
     *         this.source = source; // 上游的对象 single
     *         this.mapper = mapper; // 下游的监听对象 object: Function<Int, String>{}
     *     }
     *
     *     @Override
     *     protected void subscribeActual(final SingleObserver<? super R> t) { // t 是传入的 singleStr.subscribe() 中的 object：SingleObserver<String>{} 监听者
     *         // 让上游 single 进行订阅，订阅是 SingleMap 的内部对象
     *         source.subscribe(new MapSingleObserver<T, R>(t, mapper)); // t: object：SingleObserver<String>{}, mapper: object: Function<Int, String>{}
     *     }
     *
     *     // 1.将收到的数据进行转换，2.将转换后的数据发送到下游
     *     static final class MapSingleObserver<T, R> implements SingleObserver<T> {
     *
     *         final SingleObserver<? super R> t; // 下游的监听者 object：SingleObserver<String>{}
     *
     *         final Function<? super T, ? extends R> mapper; // object: Function<Int, String>{}
     *
     *         MapSingleObserver(SingleObserver<? super R> t, Function<? super T, ? extends R> mapper) {
     *             this.t = t;  // 下游的监听者 object：SingleObserver<String>{}
     *             this.mapper = mapper; // object: Function<Int, String>{}
     *         }
     *
     *         @Override
     *         public void onSubscribe(Disposable d) {
     *             // 回调时，回调下游 t：object：SingleObserver<String>{} 的 onSubscribe 方法
     *             t.onSubscribe(d);
     *         }
     *
     *         @Override
     *         public void onSuccess(T value) {
     *             R v;
     *             try {
     *                 // 回调时，通过 mapper.apply(value) 即 bject: Function<Int, String>{} 对上游的结果进行转化
     *                 v = Objects.requireNonNull(mapper.apply(value), "The mapper function returned a null value.");
     *             } catch (Throwable e) {
     *                 Exceptions.throwIfFatal(e);
     *                 onError(e);
     *                 return;
     *             }
     *
     *             t.onSuccess(v); // 转化之后，通过 t：object：SingleObserver<String>{} 回调到下游
     *         }
     *
     *         @Override
     *         public void onError(Throwable e) {
     *             t.onError(e); // 直接通过 t：object：SingleObserver<String>{} 回调到下游
     *         }
     *     }
     * }
     */
    private fun singleMap() {
        val single: Single<Int> = Single.just(5)

        // 整数转成 string
        val singleStr = single.map(object : Function<Int, String> {
            override fun apply(t: Int): String {
                return t.toString()
            }
        })

        singleStr.subscribe(object : SingleObserver<String> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onError(e: Throwable) {

            }

            override fun onSuccess(t: String) {
                rxTv.text = t
            }

        })
    }

    /**
     * Observable#interval
     * public static Observable<Long> interval(long initialDelay, long period, @NonNull TimeUnit unit) {
     *     return interval(initialDelay, period, unit, Schedulers.computation());
     * }
     *
     * Observable#interval
     * public static Observable<Long> interval(long initialDelay, long period, @NonNull TimeUnit unit, @NonNull Scheduler scheduler) {
     *     // 1.判空
     *     Objects.requireNonNull(unit, "unit is null");
     *     // 2.判空
     *     Objects.requireNonNull(scheduler, "scheduler is null");
     *     // 3.钩子函数，主要看 new ObservableInterval
     *     return RxJavaPlugins.onAssembly(new ObservableInterval(Math.max(0L, initialDelay), Math.max(0L, period), unit, scheduler));
     * }
     *
     * ObservableInterval#subscribeActual
     *  public void subscribeActual(Observer<? super Long> observer) {
     *      IntervalObserver is = new IntervalObserver(observer);
     *      observer.onSubscribe(is);
     *
     *      Scheduler sch = scheduler;
     *
     *      if (sch instanceof TrampolineScheduler) {
     *          Worker worker = sch.createWorker();
     *          is.setResource(worker);
     *          worker.schedulePeriodically(is, initialDelay, period, unit);
     *      } else {
     *          // is 不仅是 Disposable 还是一个 runnable，这样就会每间隔一段时间调用 runnable 的 run 方法
     *          // 定时器每秒执行 run 方法，调用下游的 onNext 方法，同时还将这个 Disposable 设置给了 IntervalObserver
     *          // 所以当 IntervalObserver 调用 dispose 时，其内部的，也就是这里的 d 取消，会导致定时器取消，也就不会发数据了
     *          Disposable d = sch.schedulePeriodicallyDirect(is, initialDelay, period, unit);
     *          // 这里会将 Disposable 设置给 IntervalObserver
     *          is.setResource(d);
     *      }
     *  }
     *
     *  // IntervalObserver 继承了 AtomicReference 有实现了 Disposable
     *  ObservableInterval
     *  static final class IntervalObserver
     *     extends AtomicReference<Disposable>
     *     implements Disposable, Runnable {
     *
     *         private static final long serialVersionUID = 346773832286157679L;
     *
     *         final Observer<? super Long> downstream;
     *
     *         long count;
     *
     *         // 这里的 downstream 就是下游的观察者
     *         IntervalObserver(Observer<? super Long> downstream) {
     *             this.downstream = downstream;
     *         }
     *
     *         @Override
     *         public void dispose() {
     *             // 取出 setResource 设置的 d，然后掉 dispose 取消
     *             DisposableHelper.dispose(this);
     *         }
     *
     *         @Override
     *         public boolean isDisposed() {
     *             return get() == DisposableHelper.DISPOSED;
     *         }
     *
     *         @Override
     *         public void run() {
     *             // 判断没有取消，就调用下游的 observer 的 onNext 方法
     *             if (get() != DisposableHelper.DISPOSED) {
     *                 downstream.onNext(count++);
     *             }
     *         }
     *
     *         // 将 d 作为内部的状态，后续取消的这是这个 d 的状态
     *         public void setResource(Disposable d) {
     *             DisposableHelper.setOnce(this, d);
     *         }
     *     }
     */
    @SuppressLint("CheckResult", "SetTextI18n")
    private fun cancelObservable() {
        // 创建一个每秒钟发出一个整数的 Observable
        val observable: Observable<Long> = Observable.interval(0, 1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io()) // 后台发送
            .observeOn(AndroidSchedulers.mainThread()) // 主线程接收

        // 订阅 Observable 并获取 Disposable 对象
        val disposable: Disposable = observable.subscribe(
            { index ->
                // onNext
                Logit.d(TAG, "cfx onNext $index")
                rxTv.text = index.toString()
            },
            { error ->
                // onError
                Logit.d(TAG, "cfx onError $error")

            },
            {
                Logit.d(TAG, "cfx onComplete")
            }
        )

        thread {
            // 等待 5s 取消 observable
            Thread.sleep(5000)
            Logit.d(TAG, "cfx cancel Observable")
            disposable.dispose()
        }

        // val disposable = Observable.interval(0, 1, TimeUnit.SECONDS)
        //     .observeOn(AndroidSchedulers.mainThread())
        //     .subscribe(object : Observer<Long> {
        //         override fun onSubscribe(d: Disposable) {
        //         }
        //
        //         override fun onError(e: Throwable) {
        //         }
        //
        //         override fun onComplete() {
        //         }
        //
        //         @SuppressLint("SetTextI18n")
        //         override fun onNext(t: Long) {
        //
        //         }
        //     })
    }

    private fun obervableMap() {
        val single = Single.just(50)

        // 整数 + 1
        val addSingle = single.map(object : Function<Int, Int> {
            override fun apply(t: Int): Int {
                return t + 1
            }
        })

        // 转字符
        val strSingle = addSingle.map(object : Function<Int, String> {
            override fun apply(t: Int): String {
                return t.toString()
            }
        })

        strSingle.subscribe(object : SingleObserver<String> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onError(e: Throwable) {

            }

            override fun onSuccess(t: String) {
                rxTv.text = t
            }

        })
    }

    /**
     * Single#delay
     * public final Single<T> delay(long time, @NonNull TimeUnit unit, @NonNull Scheduler scheduler, boolean delayError) {
     *    Objects.requireNonNull(unit, "unit is null");
     *    Objects.requireNonNull(scheduler, "scheduler is null");
     *    return RxJavaPlugins.onAssembly(new SingleDelay<>(this, time, unit, scheduler, delayError));
     * }
     *
     * SingleDelay
     * public final class SingleDelay<T> extends Single<T> {
     *
     *     final SingleSource<? extends T> source;
     *     final long time;
     *     final TimeUnit unit;
     *     final Scheduler scheduler;
     *     final boolean delayError;
     *
     *     public SingleDelay(SingleSource<? extends T> source, long time, TimeUnit unit, Scheduler scheduler, boolean delayError) {
     *         this.source = source;
     *         this.time = time;
     *         this.unit = unit;
     *         this.scheduler = scheduler;
     *         this.delayError = delayError;
     *     }
     *
     *     @Override
     *     protected void subscribeActual(final SingleObserver<? super T> observer) {
     *
     *         // 创建带延时的 Disposable
     *         final SequentialDisposable sd = new SequentialDisposable();
     *         // 下游的观察者接收观察
     *         observer.onSubscribe(sd);
     *         // 上游的订阅者进行订阅
     *         source.subscribe(new Delay(sd, observer));
     *     }
     *
     *     final class Delay implements SingleObserver<T> {
     *         private final SequentialDisposable sd;
     *         final SingleObserver<? super T> downstream;
     *
     *         Delay(SequentialDisposable sd, SingleObserver<? super T> observer) {
     *             this.sd = sd;
     *             this.downstream = observer;
     *         }
     *
     *         @Override
     *         public void onSubscribe(Disposable d) {
     *             sd.replace(d);
     *         }
     *
     *         @Override
     *         public void onSuccess(final T value) {
     *             sd.replace(scheduler.scheduleDirect(new OnSuccess(value), time, unit));
     *         }
     *
     *         @Override
     *         public void onError(final Throwable e) {
     *             sd.replace(scheduler.scheduleDirect(new OnError(e), delayError ? time : 0, unit));
     *         }
     *
     *         final class OnSuccess implements Runnable {
     *             private final T value;
     *
     *             OnSuccess(T value) {
     *                 this.value = value;
     *             }
     *
     *             @Override
     *             public void run() {
     *                 downstream.onSuccess(value);
     *             }
     *         }
     *
     *         final class OnError implements Runnable {
     *             private final Throwable e;
     *
     *             OnError(Throwable e) {
     *                 this.e = e;
     *             }
     *
     *             @Override
     *             public void run() {
     *                 downstream.onError(e);
     *             }
     *         }
     *     }
     * }
     */
    private fun singleDelay() {
        val single = Single.just(50)
        val delaySingle = single.delay(2, TimeUnit.SECONDS)

    }

    /**
     * public final <R> Observable<R> map(@NonNull Function<? super T, ? extends R> mapper) {
     *         Objects.requireNonNull(mapper, "mapper is null");
     *         return RxJavaPlugins.onAssembly(new ObservableMap<>(this, mapper));
     *     }
     *
     * public final class ObservableMap<T, U> extends AbstractObservableWithUpstream<T, U> {
     *     final Function<? super T, ? extends U> function;
     *
     *     public ObservableMap(ObservableSource<T> source, Function<? super T, ? extends U> function) {
     *         super(source);
     *         this.function = function;
     *     }
     *
     *     @Override
     *     public void subscribeActual(Observer<? super U> t) {
     *         source.subscribe(new MapObserver<T, U>(t, function));
     *     }
     *
     *     static final class MapObserver<T, U> extends BasicFuseableObserver<T, U> {
     *         final Function<? super T, ? extends U> mapper;
     *
     *         MapObserver(Observer<? super U> actual, Function<? super T, ? extends U> mapper) {
     *             super(actual);
     *             this.mapper = mapper;
     *         }
     *
     *         @Override
     *         public void onNext(T t) {
     *             if (done) {
     *                 return;
     *             }
     *
     *             if (sourceMode != NONE) {
     *                 downstream.onNext(null);
     *                 return;
     *             }
     *
     *             U v;
     *
     *             try {
     *                 v = Objects.requireNonNull(mapper.apply(t), "The mapper function returned a null value.");
     *             } catch (Throwable ex) {
     *                 fail(ex);
     *                 return;
     *             }
     *             downstream.onNext(v);
     *         }
     *
     *         @Override
     *         public int requestFusion(int mode) {
     *             return transitiveBoundaryFusion(mode);
     *         }
     *
     *         @Nullable
     *         @Override
     *         public U poll() throws Throwable {
     *             T t = qd.poll();
     *             return t != null ? Objects.requireNonNull(mapper.apply(t), "The mapper function returned a null value.") : null;
     *         }
     *     }
     * }
     *
     * BasicFuseableObserver
     *  public BasicFuseableObserver(Observer<? super R> downstream) {
     *         this.downstream = downstream;
     *     }
     *
     *     // final: fixed protocol steps to support fuseable and non-fuseable upstream
     *     @SuppressWarnings("unchecked")
     *     @Override
     *     public final void onSubscribe(Disposable d) {
     *         if (DisposableHelper.validate(this.upstream, d)) {
     *
     *             this.upstream = d;
     *             if (d instanceof QueueDisposable) {
     *                 this.qd = (QueueDisposable<T>)d;
     *             }
     *
     *             if (beforeDownstream()) {
     *
     *                 downstream.onSubscribe(this);
     *
     *                 afterDownstream();
     *             }
     *
     *         }
     *     }
     */
    private fun observerMap() {
        val observable: Observable<Long> = Observable.interval(0, 1, TimeUnit.SECONDS)
        observable.map {  }
    }

    /**
     * Observable
     * public final Observable<T> delay(long time, @NonNull TimeUnit unit) {
     *     return delay(time, unit, Schedulers.computation(), false);
     * }
     *
     * public final Observable<T> delay(long time, @NonNull TimeUnit unit, @NonNull Scheduler scheduler, boolean delayError) {
     *     Objects.requireNonNull(unit, "unit is null");
     *     Objects.requireNonNull(scheduler, "scheduler is null");
     *
     *     return RxJavaPlugins.onAssembly(new ObservableDelay<>(this, time, unit, scheduler, delayError));
     * }
     *
     * public final class ObservableDelay<T> extends AbstractObservableWithUpstream<T, T> {
     *     final long delay;
     *     final TimeUnit unit;
     *     final Scheduler scheduler;
     *     final boolean delayError;
     *
     *     public ObservableDelay(ObservableSource<T> source, long delay, TimeUnit unit, Scheduler scheduler, boolean delayError) {
     *         super(source);
     *         this.delay = delay;
     *         this.unit = unit;
     *         this.scheduler = scheduler;
     *         this.delayError = delayError;
     *     }
     *
     *     @Override
     *     @SuppressWarnings("unchecked")
     *     public void subscribeActual(Observer<? super T> t) {
     *         Observer<T> observer;
     *         if (delayError) {
     *             observer = (Observer<T>)t;
     *         } else {
     *             observer = new SerializedObserver<>(t);
     *         }
     *
     *         Scheduler.Worker w = scheduler.createWorker();
     *
     *         source.subscribe(new DelayObserver<>(observer, delay, unit, w, delayError));
     *     }
     *
     *     static final class DelayObserver<T> implements Observer<T>, Disposable {
     *         final Observer<? super T> downstream;
     *         final long delay;
     *         final TimeUnit unit;
     *         final Scheduler.Worker w;
     *         final boolean delayError;
     *
     *         Disposable upstream;
     *
     *         DelayObserver(Observer<? super T> actual, long delay, TimeUnit unit, Worker w, boolean delayError) {
     *             super();
     *             this.downstream = actual;
     *             this.delay = delay;
     *             this.unit = unit;
     *             this.w = w;
     *             this.delayError = delayError;
     *         }
     *
     *         @Override
     *         public void onSubscribe(Disposable d) {
     *             if (DisposableHelper.validate(this.upstream, d)) {
     *                 this.upstream = d;
     *                 // 调用下游
     *                 downstream.onSubscribe(this);
     *             }
     *         }
     *
     *         @Override
     *         public void onNext(final T t) {
     *             w.schedule(new OnNext(t), delay, unit);
     *         }
     *
     *         @Override
     *         public void onError(final Throwable t) {
     *             w.schedule(new OnError(t), delayError ? delay : 0, unit);
     *         }
     *
     *         @Override
     *         public void onComplete() {
     *             w.schedule(new OnComplete(), delay, unit);
     *         }
     *
     *         @Override
     *         public void dispose() {
     *             // 1.上游取消
     *             upstream.dispose();
     *             // 2.调度器取消
     *             w.dispose();
     *         }
     *
     *         @Override
     *         public boolean isDisposed() {
     *             return w.isDisposed();
     *         }
     *
     *         final class OnNext implements Runnable {
     *             private final T t;
     *
     *             OnNext(T t) {
     *                 this.t = t;
     *             }
     *
     *             @Override
     *             public void run() {
     *                 downstream.onNext(t);
     *             }
     *         }
     *
     *         final class OnError implements Runnable {
     *             private final Throwable throwable;
     *
     *             OnError(Throwable throwable) {
     *                 this.throwable = throwable;
     *             }
     *
     *             @Override
     *             public void run() {
     *                 try {
     *                     downstream.onError(throwable);
     *                 } finally {
     *                     w.dispose();
     *                 }
     *             }
     *         }
     *
     *         final class OnComplete implements Runnable {
     *             @Override
     *             public void run() {
     *                 try {
     *                     downstream.onComplete();
     *                 } finally {
     *                     w.dispose();
     *                 }
     *             }
     *         }
     *     }
     * }
     */
    private fun observerDelay() {
        val observable: Observable<Long> = Observable.interval(0, 1, TimeUnit.SECONDS)
        observable.delay(2, TimeUnit.SECONDS)
    }

    /**
     * 线程切换
     * public final Single<T> subscribeOn(@NonNull Scheduler scheduler) {
     *     Objects.requireNonNull(scheduler, "scheduler is null");
     *     // this：上游，scheduler：传入的调度器
     *     return RxJavaPlugins.onAssembly(new SingleSubscribeOn<>(this, scheduler));
     * }
     *
     * public final class SingleSubscribeOn<T> extends Single<T> {
     *     final SingleSource<? extends T> source; // 上游
     *
     *     final Scheduler scheduler; // 传入的调度器
     *
     *     public SingleSubscribeOn(SingleSource<? extends T> source, Scheduler scheduler) {
     *         this.source = source; // 上游
     *         this.scheduler = scheduler; // 传入的调度器
     *     }
     *
     *     @Override
     *     protected void subscribeActual(final SingleObserver<? super T> observer) {
     *         // observer: 下游的观察者，source：上游
     *         final SubscribeOnObserver<T> parent = new SubscribeOnObserver<>(observer, source);
     *         observer.onSubscribe(parent);
     *         // 完成线程的切换，我们先看看 parent 的 run 方法
     *         Disposable f = scheduler.scheduleDirect(parent);
     *
     *         parent.task.replace(f);
     *
     *     }
     *
     *     // 作用：1.作为内部的 runnable 作为线程切换后的订阅操作 2.作为 disposable 完成任务的取消
     *     static final class SubscribeOnObserver<T>
     *     extends AtomicReference<Disposable>
     *     implements SingleObserver<T>, Disposable, Runnable {
     *
     *         private static final long serialVersionUID = 7000911171163930287L;
     *
     *         final SingleObserver<? super T> downstream;
     *
     *         final SequentialDisposable task;
     *
     *         final SingleSource<? extends T> source;
     *
     *         SubscribeOnObserver(SingleObserver<? super T> actual, SingleSource<? extends T> source) {
     *             this.downstream = actual;
     *             this.source = source;
     *             this.task = new SequentialDisposable();
     *         }
     *
     *         @Override
     *         public void onSubscribe(Disposable d) {
     *             // 上传进来的 disposable，作为内部的 disposable
     *             DisposableHelper.setOnce(this, d);
     *         }
     *
     *         @Override
     *         public void onSuccess(T value) {
     *             downstream.onSuccess(value);
     *         }
     *
     *         @Override
     *         public void onError(Throwable e) {
     *             downstream.onError(e);
     *         }
     *
     *         @Override
     *         public void dispose() {
     *             // 取消内部的 disposable
     *             DisposableHelper.dispose(this);
     *             task.dispose();
     *         }
     *
     *         @Override
     *         public boolean isDisposed() {
     *             return DisposableHelper.isDisposed(get());
     *         }
     *
     *         @Override
     *         public void run() {
     *             // 调用上游的 subscribe 订阅，上游就完成线程的切换，所以上游往下传递的都是切完线程后的效果
     *             source.subscribe(this);
     *         }
     *     }
     *
     * }
     *
     * Single#observeOn
     * public final Single<T> observeOn(@NonNull Scheduler scheduler) {
     *     Objects.requireNonNull(scheduler, "scheduler is null");
     *     return RxJavaPlugins.onAssembly(new SingleObserveOn<>(this, scheduler));
     * }
     *
     * public final class SingleObserveOn<T> extends Single<T> {
     *
     *     final SingleSource<T> source;
     *
     *     final Scheduler scheduler;
     *
     *     public SingleObserveOn(SingleSource<T> source, Scheduler scheduler) {
     *         this.source = source;
     *         this.scheduler = scheduler;
     *     }
     *
     *     @Override
     *     protected void subscribeActual(final SingleObserver<? super T> observer) {
     *         source.subscribe(new ObserveOnSingleObserver<>(observer, scheduler));
     *     }
     *
     *     static final class ObserveOnSingleObserver<T> extends AtomicReference<Disposable>
     *     implements SingleObserver<T>, Disposable, Runnable {
     *         private static final long serialVersionUID = 3528003840217436037L;
     *
     *         final SingleObserver<? super T> downstream;
     *
     *         final Scheduler scheduler;
     *
     *         T value;
     *         Throwable error;
     *
     *         ObserveOnSingleObserver(SingleObserver<? super T> actual, Scheduler scheduler) {
     *             this.downstream = actual;
     *             this.scheduler = scheduler;
     *         }
     *
     *         @Override
     *         public void onSubscribe(Disposable d) {
     *             if (DisposableHelper.setOnce(this, d)) {
     *                 downstream.onSubscribe(this);
     *             }
     *         }
     *
     *         @Override
     *         public void onSuccess(T value) {
     *             this.value = value;
     *             // 成功时切换线程
     *             Disposable d = scheduler.scheduleDirect(this);
     *             DisposableHelper.replace(this, d);
     *         }
     *
     *         @Override
     *         public void onError(Throwable e) {
     *             this.error = e;
     *             // 失败时切换线程
     *             Disposable d = scheduler.scheduleDirect(this);
     *             DisposableHelper.replace(this, d);
     *         }
     *
     *         @Override
     *         public void run() {
     *             Throwable ex = error;
     *             if (ex != null) {
     *                 downstream.onError(ex);
     *             } else {
     *                 downstream.onSuccess(value);
     *             }
     *         }
     *
     *         @Override
     *         public void dispose() {
     *             DisposableHelper.dispose(this);
     *         }
     *
     *         @Override
     *         public boolean isDisposed() {
     *             return DisposableHelper.isDisposed(get());
     *         }
     *     }
     * }
     */
    private fun threadTransform() {
        Single.just(1)
            .subscribeOn(Schedulers.io())
            // .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }
}
