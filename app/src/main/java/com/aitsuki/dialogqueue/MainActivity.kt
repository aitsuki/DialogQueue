package com.aitsuki.dialogqueue

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

class MainActivity : AppCompatActivity() {

    private val queue = Channel<Runnable>(capacity = Channel.UNLIMITED)
    private val done = Channel<Unit>(onBufferOverflow = BufferOverflow.DROP_LATEST)
    private var activeDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.hello_btn).setOnClickListener {
            showDialogs()
        }
        lifecycleScope.launchWhenResumed {
            for (runnable in queue) {
                runnable.run()
                done.receive()
            }
        }
    }

    private fun showDialogs() {
        for (i in 0 until 10) {
            queue.trySend(Runnable {
                activeDialog = MaterialAlertDialogBuilder(this)
                    .setMessage("Hello $i")
                    .setPositiveButton("ok", null)
                    .setOnDismissListener {
                        activeDialog = null
                        done.trySend(Unit)
                    }
                    .show()
            })
        }
    }

    override fun onDestroy() {
        activeDialog?.dismiss()
        super.onDestroy()
    }
}