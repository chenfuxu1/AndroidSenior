package com.cfx.okhttp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cfx.okhttp.utils.Logit
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

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

        requestNetwork()
    }

    private fun requestNetwork() {
        val okHttpClient = OkHttpClient()
        val request = Request.Builder()
            .url(BASE_URL)
            .build()
        okHttpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Logit.d(TAG, "htd onFailure e: $e")
                }

                override fun onResponse(call: Call, response: Response) {
                    Logit.d(TAG, "htd onResponse response: ${response.body}")
                }

            })
    }
}