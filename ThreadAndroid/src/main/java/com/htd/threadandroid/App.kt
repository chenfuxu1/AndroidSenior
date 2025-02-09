package com.htd.threadandroid

import com.htd.utils.Sout

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-02-09 16:20
 *
 * Desc:
 */
fun main() {
    val customThread = CustomThread()
    customThread.start()
    Thread.sleep(3000)
    customThread.setTask {
        Sout.d("cfx 荒天帝...")
    }
    customThread.quit()
    Sout.d("cfx 退出...")
}