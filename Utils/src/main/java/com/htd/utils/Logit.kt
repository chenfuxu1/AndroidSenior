package com.htd.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * Project: CoroutineBase
 * Create By: Chen.F.X
 * DateTime: 2024-10-27 11:37
 *
 * Desc:
 */
object Logit {
    private val simpleDateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    fun d(msg: Any) {
        val formatStr = simpleDateFormat.format(Calendar.getInstance().time)
        Log.d("", "$formatStr\t${Thread.currentThread().name}\t${msg}")
    }

    fun d(tag: String, msg: Any) {
        val formatStr = simpleDateFormat.format(Calendar.getInstance().time)
        Log.d(tag, "$formatStr\t${Thread.currentThread().name}\t${msg}")
    }

    fun d(tag: String, msg: Any, throwable: Throwable) {
        val formatStr = simpleDateFormat.format(Calendar.getInstance().time)
        Log.d(tag, "$formatStr\t${Thread.currentThread().name}\t${msg}", throwable)
    }

    fun e(msg: Any) {
        val formatStr = simpleDateFormat.format(Calendar.getInstance().time)
        Log.e("", "$formatStr\t${Thread.currentThread().name}\t${msg}")
    }

    fun e(tag: String, msg: Any) {
        val formatStr = simpleDateFormat.format(Calendar.getInstance().time)
        Log.e(tag, "$formatStr\t${Thread.currentThread().name}\t${msg}")
    }

    fun e(tag: String, msg: Any, throwable: Throwable) {
        val formatStr = simpleDateFormat.format(Calendar.getInstance().time)
        Log.e(tag, "$formatStr\t${Thread.currentThread().name}\t${msg}", throwable)
    }
}