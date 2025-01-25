package com.htd.okhttp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cfx.okhttp.R
import com.htd.okhttp.utils.Logit
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.InetAddress
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val BASE_URL = "https://api.github.com/users/chenfuxu1/repos"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        testDns()
        requestNetwork()
    }

    private fun testDns() {
        thread {
            val dns = InetAddress.getAllByName("hencoder.com")
            Logit.d(TAG, "cfx dns: ${dns[0]}")
            
        }
    }

    private fun requestNetwork() {
        val okHttpClient = OkHttpClient()
        val request = Request.Builder()
            .url(BASE_URL)
            .build()
        okHttpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Logit.d(TAG, "cfx onFailure e: $e")
                }

                override fun onResponse(call: Call, response: Response) {
                    Logit.d(TAG, "cfx onResponse response: ${response.body}")
                }

            })
    }
}

/**
 * class RealCall(
 *   val client: OkHttpClient, // 参数 1：OkHttpClient 相当于通用配置大总管
 *   /** The application's original request unadulterated by redirects or auth headers. */
 *   val originalRequest: Request, // 参数 2：初始的 request，因为不是直接使用这个 request，后面还会多次封装请求
 *   val forWebSocket: Boolean // 参数 3：适用于需要频繁刷新的场景（例如股票），服务器会主动推送，一般请求使用不到
 * )
 *
 * RealCall#enqueue
 * override fun enqueue(responseCallback: Callback) {
 *     check(executed.compareAndSet(false, true)) { "Already Executed" }
 *
 *     callStart()
 *     client.dispatcher.enqueue(AsyncCall(responseCallback))
 *  }
 *
 *  RealCall#callStart
 *  private fun callStart() {
 *     // 跟踪程序运行中的错误
 *     this.callStackTrace = Platform.get().getStackTraceForCloseable("response.body().close()")
 *     // 监听各种事件，包括 http 的连接、关闭等
 *     eventListener.callStart(this)
 *  }
 *
 *  Dispatcher#enqueue
 *  // private val readyAsyncCalls = ArrayDeque<AsyncCall>()
 *  // readyAsyncCalls 是一个双向队列，里面存放着准备要执行但还没执行的请求，例如请求个数大于线程个数
 *  internal fun enqueue(call: AsyncCall) {
 *     synchronized(this) {
 *       readyAsyncCalls.add(call)
 *
 *       // Mutate the AsyncCall so that it shares the AtomicInteger of an existing running call to
 *       // the same host.
 *       // AsyncCall 是一个共享变量，里面记录请求个数
 *       // 这里主要去判断当前的 AsyncCall 要请求的主机有没有别的请求在进行，如果有，就把记录值拿过来
 *       if (!call.call.forWebSocket) {
 *         val existingCall = findExistingCallWithHost(call.host)
 *         if (existingCall != null) call.reuseCallsPerHostFrom(existingCall)
 *       }
 *     }
 *     promoteAndExecute() // 执行请求
 *   }
 *
 * // Dispatcher#promoteAndExecute
 * // 推送并且执行请求
 * private fun promoteAndExecute(): Boolean {
 *     this.assertThreadDoesntHoldLock()
 *
 *     val executableCalls = mutableListOf<AsyncCall>()
 *     val isRunning: Boolean
 *     synchronized(this) {
 *       // 1.首先挑选已经准备好的 call
 *       val i = readyAsyncCalls.iterator()
 *       while (i.hasNext()) {
 *         val asyncCall = i.next()
 *
 *         if (runningAsyncCalls.size >= this.maxRequests) break // Max capacity.
 *         if (asyncCall.callsPerHost.get() >= this.maxRequestsPerHost) continue // Host max capacity.
 *
 *         i.remove()
 *         asyncCall.callsPerHost.incrementAndGet()
 *         // 2.将挑选出来的 call 放进 executableCalls 中
 *         executableCalls.add(asyncCall)
 *         // 3.顺便放到正在执行的 runningAsyncCalls 集合中
 *         runningAsyncCalls.add(asyncCall)
 *       }
 *       isRunning = runningCallsCount() > 0
 *     }
 *
 *     // 4.将 executableCalls 遍历拿出执行
 *     for (i in 0 until executableCalls.size) {
 *       val asyncCall = executableCalls[i]
 *       asyncCall.executeOn(executorService)
 *     }
 *
 *     return isRunning
 * }
 *
 * // RealCall#executeOn 真正去执行请求
 * fun executeOn(executorService: ExecutorService) {
 *   client.dispatcher.assertThreadDoesntHoldLock()
 *
 *   var success = false
 *   try {
 *     // 这里通过线程池执行了 runnable，且这里传了 this，那说明 RealCall 一定实现了 runnable 接口
 *     // 所以我们只要看 RealCall 的 run 方法，就知道干了什么事情
 *     executorService.execute(this)
 *     success = true
 *   } catch (e: RejectedExecutionException) {
 *     val ioException = InterruptedIOException("executor rejected")
 *     ioException.initCause(e)
 *     noMoreExchanges(ioException)
 *     responseCallback.onFailure(this@RealCall, ioException)
 *   } finally {
 *     if (!success) {
 *       client.dispatcher.finished(this) // This call is no longer running!
 *     }
 *   }
 * }
 *
 * // RealCall#run
 * override fun run() {
 *   threadName("OkHttp ${redactedUrl()}") {
 *     var signalledCallback = false
 *     timeout.enter()
 *     try {
 *       // 1.拿到服务器返回的响应
 *       val response = getResponseWithInterceptorChain()
 *       signalledCallback = true
 *       // 2.回调结果，这里 responseCallback 就是我们写的匿名内部类
 *       responseCallback.onResponse(this@RealCall, response)
 *     } catch (e: IOException) {
 *       if (signalledCallback) {
 *         // Do not signal the callback twice!
 *         Platform.get().log("Callback failure for ${toLoggableString()}", Platform.INFO, e)
 *       } else {
 *         responseCallback.onFailure(this@RealCall, e)
 *       }
 *     } catch (t: Throwable) {
 *       cancel()
 *       if (!signalledCallback) {
 *         val canceledException = IOException("canceled due to $t")
 *         canceledException.addSuppressed(t)
 *         responseCallback.onFailure(this@RealCall, canceledException)
 *       }
 *       throw t
 *     } finally {
 *       client.dispatcher.finished(this)
 *     }
 *   }
 * }
 */

/**
 * OkHttpClient
 * open class OkHttpClient internal constructor(
 *   builder: Builder
 * ) : Cloneable, Call.Factory, WebSocket.Factory {
 *
 *   // 线程调度器
 *   @get:JvmName("dispatcher") val dispatcher: Dispatcher = builder.dispatcher
 *   // 连接池，批量管理对象，通过重用和回收来追求资源和性能的动态平衡
 *   @get:JvmName("connectionPool") val connectionPool: ConnectionPool = builder.connectionPool
 *
 *   @get:JvmName("interceptors") val interceptors: List<Interceptor> =
 *       builder.interceptors.toImmutableList()
 *
 *   @get:JvmName("networkInterceptors") val networkInterceptors: List<Interceptor> =
 *       builder.networkInterceptors.toImmutableList()
 *
 *   // 各种事件的监听器
 *   @get:JvmName("eventListenerFactory") val eventListenerFactory: EventListener.Factory =
 *       builder.eventListenerFactory
 *
 *   // 请求失败、连接失败的重试，默认是 true，表示默认会重试
 *   @get:JvmName("retryOnConnectionFailure") val retryOnConnectionFailure: Boolean =
 *       builder.retryOnConnectionFailure
 *
 *   // 自动去修正
 *   @get:JvmName("authenticator") val authenticator: Authenticator = builder.authenticator
 *
 *   // 是否响应跟随重定向，一般默认是打开 true
 *   @get:JvmName("followRedirects") val followRedirects: Boolean = builder.followRedirects
 *
 *   // 当 followRedirects 开关打开时，反生 ssl 切换，是否需要重定向，例如从 http -> https 的切换，一般默认是打开 true
 *   @get:JvmName("followSslRedirects") val followSslRedirects: Boolean = builder.followSslRedirects
 *
 *   // cookie 的缓存，一般浏览器使用
 *   @get:JvmName("cookieJar") val cookieJar: CookieJar = builder.cookieJar
 *
 *   //
 *   @get:JvmName("cache") val cache: Cache? = builder.cache
 *
 *   // dns 域名解析
 *   @get:JvmName("dns") val dns: Dns = builder.dns
 *
 *   // 代理
 *   @get:JvmName("proxy") val proxy: Proxy? = builder.proxy
 *
 *   @get:JvmName("proxySelector") val proxySelector: ProxySelector =
 *       when {
 *         // Defer calls to ProxySelector.getDefault() because it can throw a SecurityException.
 *         builder.proxy != null -> NullProxySelector
 *         else -> builder.proxySelector ?: ProxySelector.getDefault() ?: NullProxySelector
 *       }
 *
 *   @get:JvmName("proxyAuthenticator") val proxyAuthenticator: Authenticator =
 *       builder.proxyAuthenticator
 *
 *   @get:JvmName("socketFactory") val socketFactory: SocketFactory = builder.socketFactory
 *
 *   private val sslSocketFactoryOrNull: SSLSocketFactory?
 *
 *   @get:JvmName("sslSocketFactory") val sslSocketFactory: SSLSocketFactory
 *     get() = sslSocketFactoryOrNull ?: throw IllegalStateException("CLEARTEXT-only client")
 *
 *   @get:JvmName("x509TrustManager") val x509TrustManager: X509TrustManager?
 *
 *   @get:JvmName("connectionSpecs") val connectionSpecs: List<ConnectionSpec> =
 *       builder.connectionSpecs
 *
 *   @get:JvmName("protocols") val protocols: List<Protocol> = builder.protocols
 *
 *   @get:JvmName("hostnameVerifier") val hostnameVerifier: HostnameVerifier = builder.hostnameVerifier
 *
 *   @get:JvmName("certificatePinner") val certificatePinner: CertificatePinner
 *
 *   @get:JvmName("certificateChainCleaner") val certificateChainCleaner: CertificateChainCleaner?
 *
 *   /**
 *    * Default call timeout (in milliseconds). By default there is no timeout for complete calls, but
 *    * there is for the connect, write, and read actions within a call.
 *    */
 *   @get:JvmName("callTimeoutMillis") val callTimeoutMillis: Int = builder.callTimeout
 *
 *   /** Default connect timeout (in milliseconds). The default is 10 seconds. */
 *   @get:JvmName("connectTimeoutMillis") val connectTimeoutMillis: Int = builder.connectTimeout
 *
 *   /** Default read timeout (in milliseconds). The default is 10 seconds. */
 *   @get:JvmName("readTimeoutMillis") val readTimeoutMillis: Int = builder.readTimeout
 *
 *   /** Default write timeout (in milliseconds). The default is 10 seconds. */
 *   @get:JvmName("writeTimeoutMillis") val writeTimeoutMillis: Int = builder.writeTimeout
 *
 *   /** Web socket and HTTP/2 ping interval (in milliseconds). By default pings are not sent. */
 *   @get:JvmName("pingIntervalMillis") val pingIntervalMillis: Int = builder.pingInterval
 *
 *   /**
 *    * Minimum outbound web socket message size (in bytes) that will be compressed.
 *    * The default is 1024 bytes.
 *    */
 *   @get:JvmName("minWebSocketMessageToCompress")
 *   val minWebSocketMessageToCompress: Long = builder.minWebSocketMessageToCompress
 *
 *   val routeDatabase: RouteDatabase = builder.routeDatabase ?: RouteDatabase()
 * }
 */

