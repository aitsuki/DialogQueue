package com.aitsuki.dialogqueue

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private val queue = DialogQueue(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.hello_btn).setOnClickListener {
            // 将以 1 4 3 2 的顺序显示，因为此时队列为空，第一个入列的dialog会立马显示。
            // 队列阻塞后，其余的dialog才能根据优先级排序
            showDialogs(4)
        }
        // 将以 3 2 1 的顺序显示
        showDialogs(3)
    }

    private fun showDialogs(n: Int) {
        for (i in 1..n) {
            //                  反向显示
            queue.offer("$i", n - i) { next ->
                MaterialAlertDialogBuilder(this)
                    .setMessage("Hello $i")
                    .setPositiveButton("ok", null)
                    .setOnDismissListener { next() }.create()
            }
        }
    }
}