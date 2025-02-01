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
 *   // 代理验证机制
 *   @get:JvmName("proxyAuthenticator") val proxyAuthenticator: Authenticator =
 *       builder.proxyAuthenticator
 *
 *   // 创建 socket 的工厂
 *   @get:JvmName("socketFactory") val socketFactory: SocketFactory = builder.socketFactory
 *
 *   private val sslSocketFactoryOrNull: SSLSocketFactory?
 *
 *   // 创建 ssl socket 的工厂
 *   @get:JvmName("sslSocketFactory") val sslSocketFactory: SSLSocketFactory
 *     get() = sslSocketFactoryOrNull ?: throw IllegalStateException("CLEARTEXT-only client")
 *
 *   // 证书验证的 manager
 *   @get:JvmName("x509TrustManager") val x509TrustManager: X509TrustManager?
 *
 *   // 装载各种加密套件信息
 *   @get:JvmName("connectionSpecs") val connectionSpecs: List<ConnectionSpec> =
 *       builder.connectionSpecs
 *
 *   // 支持的所有协议
 *   @get:JvmName("protocols") val protocols: List<Protocol> = builder.protocols
 *
 *   // 验证证书
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
 *   // 心跳间隔时间
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

/**
 * internal fun getResponseWithInterceptorChain(): Response {
 *     // Build a full stack of interceptors.
 *     val interceptors = mutableListOf<Interceptor>()
 *     interceptors += client.interceptors
 *     interceptors += RetryAndFollowUpInterceptor(client)
 *     interceptors += BridgeInterceptor(client.cookieJar)
 *     interceptors += CacheInterceptor(client.cache)
 *     interceptors += ConnectInterceptor
 *     if (!forWebSocket) {
 *       interceptors += client.networkInterceptors
 *     }
 *     interceptors += CallServerInterceptor(forWebSocket)
 *
 *     val chain = RealInterceptorChain(
 *         call = this,
 *         interceptors = interceptors,
 *         index = 0,
 *         exchange = null,
 *         request = originalRequest,
 *         connectTimeoutMillis = client.connectTimeoutMillis,
 *         readTimeoutMillis = client.readTimeoutMillis,
 *         writeTimeoutMillis = client.writeTimeoutMillis
 *     )
 *
 *     var calledNoMoreExchanges = false
 *     try {
 *       val response = chain.proceed(originalRequest)
 *       if (isCanceled()) {
 *         response.closeQuietly()
 *         throw IOException("Canceled")
 *       }
 *       return response
 *     } catch (e: IOException) {
 *       calledNoMoreExchanges = true
 *       throw noMoreExchanges(e) as Throwable
 *     } finally {
 *       if (!calledNoMoreExchanges) {
 *         noMoreExchanges(null)
 *       }
 *     }
 *   }
 *
 * // RetryAndFollowUpInterceptor#intercept 请求重试
 * override fun intercept(chain: Interceptor.Chain): Response {
 *     // 1.前置操作
 *     val realChain = chain as RealInterceptorChain
 *     var request = chain.request
 *     val call = realChain.call
 *     var followUpCount = 0
 *     var priorResponse: Response? = null
 *     var newExchangeFinder = true
 *     var recoveredFailures = listOf<IOException>()
 *     while (true) {
 *       // 1.1 连接前的准备工作
 *       call.enterNetworkInterceptorExchange(request, newExchangeFinder)
 *
 *       var response: Response
 *       var closeActiveExchange = true
 *       try {
 *         if (call.isCanceled()) {
 *           throw IOException("Canceled")
 *         }
 *
 *         try {
 *           // 2.将链条传送到下个 Interceptor 执行
 *           response = realChain.proceed(request)
 *           // 3.RetryAndFollowUpInterceptor 的后置操作
 *           newExchangeFinder = true
 *         } catch (e: RouteException) {
 *           // The attempt to connect via a route failed. The request will not have been sent.
 *           // 路由失败了，进行重试
 *           if (!recover(e.lastConnectException, call, request, requestSendStarted = false)) {
 *             throw e.firstConnectException.withSuppressed(recoveredFailures)
 *           } else {
 *             recoveredFailures += e.firstConnectException
 *           }
 *           newExchangeFinder = false
 *           continue
 *         } catch (e: IOException) {
 *           // An attempt to communicate with a server failed. The request may have been sent.
 *           if (!recover(e, call, request, requestSendStarted = e !is ConnectionShutdownException)) {
 *             throw e.withSuppressed(recoveredFailures)
 *           } else {
 *             recoveredFailures += e
 *           }
 *           newExchangeFinder = false
 *           continue
 *         }
 *
 *         // Attach the prior response if it exists. Such responses never have a body.
 *         // 4.没出错，可能会发生重定向，状态码不是 200
 *         if (priorResponse != null) {
 *           response = response.newBuilder()
 *               .priorResponse(priorResponse.newBuilder()
 *                   .body(null)
 *                   .build())
 *               .build()
 *         }
 *
 *         val exchange = call.interceptorScopedExchange
 *         val followUp = followUpRequest(response, exchange)
 *
 *         if (followUp == null) {
 *           if (exchange != null && exchange.isDuplex) {
 *             call.timeoutEarlyExit()
 *           }
 *           closeActiveExchange = false
 *           return response
 *         }
 *
 *         val followUpBody = followUp.body
 *         if (followUpBody != null && followUpBody.isOneShot()) {
 *           closeActiveExchange = false
 *           return response
 *         }
 *
 *         response.body?.closeQuietly()
 *
 *         if (++followUpCount > MAX_FOLLOW_UPS) {
 *           throw ProtocolException("Too many follow-up requests: $followUpCount")
 *         }
 *
 *         request = followUp
 *         priorResponse = response
 *       } finally {
 *         call.exitNetworkInterceptorExchange(closeActiveExchange)
 *       }
 *     }
 *   }
 *
 * // RetryAndFollowUpInterceptor#intercept#enterNetworkInterceptorExchange
 * // 连接前的准备工作
 * fun enterNetworkInterceptorExchange(request: Request, newExchangeFinder: Boolean) {
 *     check(interceptorScopedExchange == null)
 *
 *     synchronized(this) {
 *       check(!responseBodyOpen) {
 *         "cannot make a new request because the previous response is still open: " +
 *             "please call response.close()"
 *       }
 *       check(!requestBodyOpen)
 *     }
 *
 *     if (newExchangeFinder) {
 *       // 主要是创建了一个 ExchangeFinder 数据交换（可用的连接以及连接的各种参数）
 *       this.exchangeFinder = ExchangeFinder(
 *           connectionPool,
 *           createAddress(request.url),
 *           this,
 *           eventListener
 *       )
 *     }
 *   }
 *
 * // RetryAndFollowUpInterceptor#recover
 * private fun recover(
 *     e: IOException,
 *     call: RealCall,
 *     userRequest: Request,
 *     requestSendStarted: Boolean
 *   ): Boolean {
 *     // The application layer has forbidden retries.
 *     // 1.先看设置的 retryOnConnectionFailure 是否为 false， 默认是 true，如果不需要重试，直接返回 false
 *     if (!client.retryOnConnectionFailure) return false
 *
 *     // We can't send the request body again.
 *     if (requestSendStarted && requestIsOneShot(e, userRequest)) return false
 *
 *     // This exception is fatal.
 *     // 2.判断抛出的异常是否是可以修复的，如果不能修复，直接返回 false
 *     if (!isRecoverable(e, requestSendStarted)) return false
 *
 *     // No more routes to attempt.
 *     if (!call.retryAfterFailure()) return false
 *
 *     // For failure recovery, use the same route selector with a new connection.
 *     return true
 *   }
 *
 * // BridgeInterceptor 主要用于连接当前已经准备好的连接和即将发送的连接
 * // 主要用于添加各种 header
 * override fun intercept(chain: Interceptor.Chain): Response {
 *     val userRequest = chain.request()
 *     val requestBuilder = userRequest.newBuilder()
 *
 *     val body = userRequest.body
 *     if (body != null) {
 *       // 1.添加各种 header
 *       val contentType = body.contentType()
 *       if (contentType != null) {
 *         requestBuilder.header("Content-Type", contentType.toString())
 *       }
 *
 *       val contentLength = body.contentLength()
 *       if (contentLength != -1L) {
 *         requestBuilder.header("Content-Length", contentLength.toString())
 *         requestBuilder.removeHeader("Transfer-Encoding")
 *       } else {
 *         requestBuilder.header("Transfer-Encoding", "chunked")
 *         requestBuilder.removeHeader("Content-Length")
 *       }
 *     }
 *
 *     if (userRequest.header("Host") == null) {
 *       requestBuilder.header("Host", userRequest.url.toHostHeader())
 *     }
 *
 *     if (userRequest.header("Connection") == null) {
 *       requestBuilder.header("Connection", "Keep-Alive")
 *     }
 *
 *     // If we add an "Accept-Encoding: gzip" header field we're responsible for also decompressing
 *     // the transfer stream.
 *     var transparentGzip = false
 *     if (userRequest.header("Accept-Encoding") == null && userRequest.header("Range") == null) {
 *       transparentGzip = true
 *       requestBuilder.header("Accept-Encoding", "gzip")
 *     }
 *
 *     val cookies = cookieJar.loadForRequest(userRequest.url)
 *     if (cookies.isNotEmpty()) {
 *       requestBuilder.header("Cookie", cookieHeader(cookies))
 *     }
 *
 *     if (userRequest.header("User-Agent") == null) {
 *       requestBuilder.header("User-Agent", userAgent)
 *     }
 *
 *     val networkResponse = chain.proceed(requestBuilder.build())
 *
 *     cookieJar.receiveHeaders(userRequest.url, networkResponse.headers)
 *
 *     val responseBuilder = networkResponse.newBuilder()
 *         .request(userRequest)
 *
 *     if (transparentGzip &&
 *         "gzip".equals(networkResponse.header("Content-Encoding"), ignoreCase = true) &&
 *         networkResponse.promisesBody()) {
 *       val responseBody = networkResponse.body
 *       if (responseBody != null) {
 *         val gzipSource = GzipSource(responseBody.source())
 *         val strippedHeaders = networkResponse.headers.newBuilder()
 *             .removeAll("Content-Encoding")
 *             .removeAll("Content-Length")
 *             .build()
 *         responseBuilder.headers(strippedHeaders)
 *         val contentType = networkResponse.header("Content-Type")
 *         responseBuilder.body(RealResponseBody(contentType, -1L, gzipSource.buffer()))
 *       }
 *     }
 *
 *     return responseBuilder.build()
 *   }
 *
 * // CacheInterceptor 用于结果缓存
 * override fun intercept(chain: Interceptor.Chain): Response {
 *     val call = chain.call()
 *     val cacheCandidate = cache?.get(chain.request())
 *
 *     val now = System.currentTimeMillis()
 *
 *     val strategy = CacheStrategy.Factory(now, chain.request(), cacheCandidate).compute()
 *     val networkRequest = strategy.networkRequest
 *     val cacheResponse = strategy.cacheResponse
 *
 *     cache?.trackResponse(strategy)
 *     val listener = (call as? RealCall)?.eventListener ?: EventListener.NONE
 *
 *     if (cacheCandidate != null && cacheResponse == null) {
 *       // The cache candidate wasn't applicable. Close it.
 *       cacheCandidate.body?.closeQuietly()
 *     }
 *
 *     // If we're forbidden from using the network and the cache is insufficient, fail.
 *     if (networkRequest == null && cacheResponse == null) {
 *       return Response.Builder()
 *           .request(chain.request())
 *           .protocol(Protocol.HTTP_1_1)
 *           .code(HTTP_GATEWAY_TIMEOUT)
 *           .message("Unsatisfiable Request (only-if-cached)")
 *           .body(EMPTY_RESPONSE)
 *           .sentRequestAtMillis(-1L)
 *           .receivedResponseAtMillis(System.currentTimeMillis())
 *           .build().also {
 *             listener.satisfactionFailure(call, it)
 *           }
 *     }
 *
 *     // If we don't need the network, we're done.
 *     // 1.前置工作，先看看缓存中有没有可用的 cache，如果有，直接返回
 *     if (networkRequest == null) {
 *       return cacheResponse!!.newBuilder()
 *           .cacheResponse(stripBody(cacheResponse))
 *           .build().also {
 *             listener.cacheHit(call, it)
 *           }
 *     }
 *
 *     if (cacheResponse != null) {
 *       listener.cacheConditionalHit(call, cacheResponse)
 *     } else if (cache != null) {
 *       listener.cacheMiss(call)
 *     }
 *
 *     var networkResponse: Response? = null
 *     try {
 *       // 2.中置工作，链式发起网络请求
 *       networkResponse = chain.proceed(networkRequest)
 *     } finally {
 *       // If we're crashing on I/O or otherwise, don't leak the cache body.
 *       if (networkResponse == null && cacheCandidate != null) {
 *         cacheCandidate.body?.closeQuietly()
 *       }
 *     }
 *
 *     // If we have a cache response too, then we're doing a conditional get.
 *     // 3.后置工作，判断是否需要将数据缓存下来
 *     if (cacheResponse != null) {
 *       if (networkResponse?.code == HTTP_NOT_MODIFIED) {
 *         val response = cacheResponse.newBuilder()
 *             .headers(combine(cacheResponse.headers, networkResponse.headers))
 *             .sentRequestAtMillis(networkResponse.sentRequestAtMillis)
 *             .receivedResponseAtMillis(networkResponse.receivedResponseAtMillis)
 *             .cacheResponse(stripBody(cacheResponse))
 *             .networkResponse(stripBody(networkResponse))
 *             .build()
 *
 *         networkResponse.body!!.close()
 *
 *         // Update the cache after combining headers but before stripping the
 *         // Content-Encoding header (as performed by initContentStream()).
 *         cache!!.trackConditionalCacheHit()
 *         cache.update(cacheResponse, response)
 *         return response.also {
 *           listener.cacheHit(call, it)
 *         }
 *       } else {
 *         cacheResponse.body?.closeQuietly()
 *       }
 *     }
 *
 *     val response = networkResponse!!.newBuilder()
 *         .cacheResponse(stripBody(cacheResponse))
 *         .networkResponse(stripBody(networkResponse))
 *         .build()
 *
 *     if (cache != null) {
 *       if (response.promisesBody() && CacheStrategy.isCacheable(response, networkRequest)) {
 *         // Offer this request to the cache.
 *         val cacheRequest = cache.put(response)
 *         return cacheWritingResponse(cacheRequest, response).also {
 *           if (cacheResponse != null) {
 *             // This will log a conditional cache miss only.
 *             listener.cacheMiss(call)
 *           }
 *         }
 *       }
 *
 *       if (HttpMethod.invalidatesCache(networkRequest.method)) {
 *         try {
 *           cache.remove(networkRequest)
 *         } catch (_: IOException) {
 *           // The cache cannot be written.
 *         }
 *       }
 *     }
 *
 *     return response
 *   }
 *
 * // ConnectInterceptor 最重要的
 * override fun intercept(chain: Interceptor.Chain): Response {
 *     // 1.前置工作，创建连接
 *     val realChain = chain as RealInterceptorChain
 *     val exchange = realChain.call.initExchange(chain)
 *     val connectedChain = realChain.copy(exchange = exchange)
 *     // 2.中置工作，发起请求（无后置工作）
 *     return connectedChain.proceed(realChain.request)
 *   }
 *
 * // RealCall#initExchange
 * internal fun initExchange(chain: RealInterceptorChain): Exchange {
 *     synchronized(this) {
 *       check(expectMoreExchanges) { "released" }
 *       check(!responseBodyOpen)
 *       check(!requestBodyOpen)
 *     }
 *
 *     val exchangeFinder = this.exchangeFinder!!
 *     val codec = exchangeFinder.find(client, chain)
 *     val result = Exchange(this, eventListener, exchangeFinder, codec)
 *     this.interceptorScopedExchange = result
 *     this.exchange = result
 *     synchronized(this) {
 *       this.requestBodyOpen = true
 *       this.responseBodyOpen = true
 *     }
 *
 *     if (canceled) throw IOException("Canceled")
 *     return result
 *   }
 *
 * // ExchangeFinder#find
 * fun find(
 *     client: OkHttpClient,
 *     chain: RealInterceptorChain
 *   ): ExchangeCodec {
 *     try {
 *       // 1.先拿到可用的连接
 *       val resultConnection = findHealthyConnection(
 *           connectTimeout = chain.connectTimeoutMillis,
 *           readTimeout = chain.readTimeoutMillis,
 *           writeTimeout = chain.writeTimeoutMillis,
 *           pingIntervalMillis = client.pingIntervalMillis,
 *           connectionRetryEnabled = client.retryOnConnectionFailure,
 *           doExtensiveHealthChecks = chain.request.method != "GET"
 *       )
 *       // 2.基于可用连接拿到编码格式
 *       return resultConnection.newCodec(client, chain)
 *     } catch (e: RouteException) {
 *       trackFailure(e.lastConnectException)
 *       throw e
 *     } catch (e: IOException) {
 *       trackFailure(e)
 *       throw RouteException(e)
 *     }
 *   }
 *
 * // ExchangeFinder#findHealthyConnection 找到一个健康的可用连接
 * private fun findHealthyConnection(
 *     connectTimeout: Int,
 *     readTimeout: Int,
 *     writeTimeout: Int,
 *     pingIntervalMillis: Int,
 *     connectionRetryEnabled: Boolean,
 *     doExtensiveHealthChecks: Boolean
 *   ): RealConnection {
 *     while (true) {
 *       // 1.先拿到一个可用连接
 *       val candidate = findConnection(
 *           connectTimeout = connectTimeout,
 *           readTimeout = readTimeout,
 *           writeTimeout = writeTimeout,
 *           pingIntervalMillis = pingIntervalMillis,
 *           connectionRetryEnabled = connectionRetryEnabled
 *       )
 *
 *       // Confirm that the connection is good.
 *       // 2.验证是否健康，如果不健康，就继续寻找连接，如果健康，就返回
 *       if (candidate.isHealthy(doExtensiveHealthChecks)) {
 *         return candidate
 *       }
 *
 *       // If it isn't, take it out of the pool.
 *       candidate.noNewExchanges()
 *
 *       // Make sure we have some routes left to try. One example where we may exhaust all the routes
 *       // would happen if we made a new connection and it immediately is detected as unhealthy.
 *       if (nextRouteToTry != null) continue
 *
 *       val routesLeft = routeSelection?.hasNext() ?: true
 *       if (routesLeft) continue
 *
 *       val routesSelectionLeft = routeSelector?.hasNext() ?: true
 *       if (routesSelectionLeft) continue
 *
 *       throw IOException("exhausted all routes")
 *     }
 *   }
 *
 * // ExchangeFinder#findConnection 寻找可用连接
 * // 最多情况下，会通过 5 种方式获取连接
 * private fun findConnection(
 *     connectTimeout: Int,
 *     readTimeout: Int,
 *     writeTimeout: Int,
 *     pingIntervalMillis: Int,
 *     connectionRetryEnabled: Boolean
 *   ): RealConnection {
 *     // 1.如果 call 已经被取消了，那么直接抛出异常
 *     if (call.isCanceled()) throw IOException("Canceled")
 *
 *     // Attempt to reuse the connection from the call.
 *     // 第一次还没有连接 callConnection 为空，先不看，只拿不多路复用的连接
 *     val callConnection = call.connection // This may be mutated by releaseConnectionNoEvents()!
 *     if (callConnection != null) {
 *       var toClose: Socket? = null
 *       synchronized(callConnection) {
 *         // 如果要使用新的请求，就将上次的请求关闭
 *         if (callConnection.noNewExchanges || !sameHostAndPort(callConnection.route().address.url)) {
 *           toClose = call.releaseConnectionNoEvents()
 *         }
 *       }
 *
 *       // If the call's connection wasn't released, reuse it. We don't call connectionAcquired() here
 *       // because we already acquired it.
 *       // 第五次，如果是重定向的，直接获取
 *       if (call.connection != null) {
 *         check(toClose == null)
 *         return callConnection
 *       }
 *
 *       // The call's connection was released.
 *       toClose?.closeQuietly()
 *       eventListener.connectionReleased(call, callConnection)
 *     }
 *
 *     // We need a new connection. Give it fresh stats.
 *     refusedStreamCount = 0
 *     connectionShutdownCount = 0
 *     otherFailureCount = 0
 *
 *     // Attempt to get a connection from the pool.
 *     // 第一种，直接从池里拿到可用连接
 *     if (connectionPool.callAcquirePooledConnection(address, call, null, false)) {
 *       val result = call.connection!!
 *       eventListener.connectionAcquired(call, result)
 *       return result
 *     }
 *
 *     // Nothing in the pool. Figure out what route we'll try next.
 *     val routes: List<Route>?
 *     val route: Route
 *     if (nextRouteToTry != null) {
 *       // Use a route from a preceding coalesced connection.
 *       routes = null
 *       route = nextRouteToTry!!
 *       nextRouteToTry = null
 *     } else if (routeSelection != null && routeSelection!!.hasNext()) {
 *       // Use a route from an existing route selection.
 *       routes = null
 *       route = routeSelection!!.next()
 *     } else {
 *       // Compute a new route selection. This is a blocking operation!
 *       var localRouteSelector = routeSelector
 *       if (localRouteSelector == null) {
 *         localRouteSelector = RouteSelector(address, call.client.routeDatabase, call, eventListener)
 *         this.routeSelector = localRouteSelector
 *       }
 *       val localRouteSelection = localRouteSelector.next()
 *       routeSelection = localRouteSelection
 *       routes = localRouteSelection.routes
 *
 *       if (call.isCanceled()) throw IOException("Canceled")
 *
 *       // Now that we have a set of IP addresses, make another attempt at getting a connection from
 *       // the pool. We have a better chance of matching thanks to connection coalescing.
 *       // 第二次，再从连接池中获取一遍，这次会传入 routes，能拿到范围更广的连接（包含 http1、http2，多路复用的）
 *       // 多路、不多路的连接都拿
 *       if (connectionPool.callAcquirePooledConnection(address, call, routes, false)) {
 *         val result = call.connection!!
 *         eventListener.connectionAcquired(call, result)
 *         return result
 *       }
 *
 *       route = localRouteSelection.next()
 *     }
 *
 *     // Connect. Tell the call about the connecting call so async cancels work.
 *     val newConnection = RealConnection(connectionPool, route)
 *     call.connectionToCancel = newConnection
 *     try {
 *       // 第三次，如果第一二次都没拿到，就自己创建
 *       newConnection.connect(
 *           connectTimeout,
 *           readTimeout,
 *           writeTimeout,
 *           pingIntervalMillis,
 *           connectionRetryEnabled,
 *           call,
 *           eventListener
 *       )
 *     } finally {
 *       call.connectionToCancel = null
 *     }
 *     call.client.routeDatabase.connected(newConnection.route())
 *
 *     // If we raced another call connecting to this host, coalesce the connections. This makes for 3
 *     // different lookups in the connection pool!
 *
 *     // 第四次，再尝试从连接池获取一次，会传入 routes、true 需要多路复用的连接
 *     // 只拿到 http2 多路复用的连接，防止第一次创建了后续的重复创建的连接，两次都创建了，只拿第一次创建的，后续的扔掉
 *     if (connectionPool.callAcquirePooledConnection(address, call, routes, true)) {
 *       val result = call.connection!!
 *       nextRouteToTry = route
 *       newConnection.socket().closeQuietly()
 *       eventListener.connectionAcquired(call, result)
 *       return result
 *     }
 *
 *     synchronized(newConnection) {
 *       connectionPool.put(newConnection)
 *       call.acquireConnectionNoEvents(newConnection)
 *     }
 *
 *     eventListener.connectionAcquired(call, newConnection)
 *     return newConnection
 *   }
 *
 * // RealConnectionPool#callAcquirePooledConnection
 * fun callAcquirePooledConnection(
 *     address: Address,
 *     call: RealCall,
 *     routes: List<Route>?,
 *     requireMultiplexed: Boolean
 *   ): Boolean {
 *     // 遍历连接池
 *     for (connection in connections) {
 *       synchronized(connection) {
 *         // 判断连接是否是多路复用的
 *         if (requireMultiplexed && !connection.isMultiplexed) return@synchronized
 *         // 判断连接是否是可用的
 *         if (!connection.isEligible(address, routes)) return@synchronized
 *         // 拿到这个可用连接，并且不发事件
 *         call.acquireConnectionNoEvents(connection)
 *         return true
 *       }
 *     }
 *     return false
 *   }
 *
 * RealConnection#isEligible 判断连接是否是可用的
 * // 1 连接数没有超限 2 需要相同的路线连接的主机
 * internal fun isEligible(address: Address, routes: List<Route>?): Boolean {
 *     assertThreadHoldsLock()
 *
 *     // If this connection is not accepting new exchanges, we're done.
 *     // 1.判断请求的数量是否超限，http2 以下的最大请求是 1，noNewExchanges：是否愿意接收新的请求
 *     if (calls.size >= allocationLimit || noNewExchanges) return false
 *
 *     // If the non-host fields of the address don't overlap, we're done.
 *     // 2.比较两次请求的主机是否相同
 *     if (!this.route.address.equalsNonHost(address)) return false
 *
 *     // If the host exactly matches, we're done: this connection can carry the address.
 *     if (address.url.host == this.route().address.url.host) {
 *       return true // This connection is a perfect match.
 *     }
 *
 *     // At this point we don't have a hostname match. But we still be able to carry the request if
 *     // our connection coalescing requirements are met. See also:
 *     // https://hpbn.co/optimizing-application-delivery/#eliminate-domain-sharding
 *     // https://daniel.haxx.se/blog/2016/08/18/http2-connection-coalescing/
 *
 *     // 1. This connection must be HTTP/2.
 *     if (http2Connection == null) return false
 *
 *     // 2. The routes must share an IP address.
 *     // 判断 ip、代理是否一样
 *     if (routes == null || !routeMatchesAny(routes)) return false
 *
 *     // 3. This connection's server certificate's must cover the new host.
 *     // 判断证书是否一样
 *     if (address.hostnameVerifier !== OkHostnameVerifier) return false
 *     if (!supportsUrl(address.url)) return false
 *
 *     // 4. Certificate pinning must match the host.
 *     try {
 *       address.certificatePinner!!.check(address.url.host, handshake()!!.peerCertificates)
 *     } catch (_: SSLPeerUnverifiedException) {
 *       return false
 *     }
 *
 *     return true // The caller's address can be carried by this connection.
 *   }
 *
 * CallServerInterceptor 发送请求，读取响应
 */
