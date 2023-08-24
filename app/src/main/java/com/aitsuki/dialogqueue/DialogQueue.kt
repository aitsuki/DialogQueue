package com.aitsuki.dialogqueue

import android.app.Dialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import java.util.PriorityQueue

class DialogQueue(private val lifecycleOwner: LifecycleOwner) {

    private var activeDialog: Dialog? = null

    private val pq = PriorityQueue<Task>(11, Comparator { o1, o2 ->
        return@Comparator o1.priority - o2.priority
    })

    init {
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                tryPop()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                activeDialog?.dismiss()
                activeDialog = null
            }
        })
    }

    private fun tryPop() {
        if (activeDialog == null && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            val task = pq.poll() ?: return
            val nextFunc = fun() {
                activeDialog = null
                tryPop()
            }
            activeDialog = task.dialogBuilder(nextFunc)
            activeDialog?.show()
        }
    }

    fun offer(tag: String, priority: Int, dialogBuilder: (next: () -> Unit) -> Dialog) {
        val task = Task(tag, priority, dialogBuilder)
        if (pq.contains(task)) {
            return
        }
        pq.offer(task)
        tryPop()
    }

    private class Task(
        val tag: String,
        val priority: Int,
        val dialogBuilder: (next: () -> Unit) -> Dialog
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Task
            if (tag != other.tag) return false
            return true
        }

        override fun hashCode(): Int {
            return tag.hashCode()
        }
    }
}