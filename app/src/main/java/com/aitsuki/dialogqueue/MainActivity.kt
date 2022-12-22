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
            showDialogsReverseOrder(10)
        }
        showDialogsReverseOrder(3)
    }

    // 反向显示
    private fun showDialogsReverseOrder(n: Int) {
        for (i in 1..n) {
            queue.offer("$i", n - i) { next ->
                MaterialAlertDialogBuilder(this)
                    .setMessage("Hello $i")
                    .setPositiveButton("ok", null)
                    .setOnDismissListener { next() }.create()
            }
        }
    }
}