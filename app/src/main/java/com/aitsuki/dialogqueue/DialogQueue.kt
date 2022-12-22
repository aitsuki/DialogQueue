package com.aitsuki.dialogqueue

import android.app.Dialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import java.util.*

class DialogQueue(lifecycleOwner: LifecycleOwner) {

    private val queue = Channel<Unit>(Channel.UNLIMITED)
    private val next = Channel<Unit>(onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val pq = PriorityQueue<Task>(11, Comparator { o1, o2 ->
        return@Comparator o2.priority - o1.priority
    })

    init {
        var activeDialog: Dialog? = null
        lifecycleOwner.lifecycleScope.launchWhenResumed {
            for (Unit in queue) {
                val task = pq.peek() ?: continue
                val nextFunc = fun() {
                    activeDialog = null
                    pq.remove(task)
                    next.trySend(Unit)
                }
                try {
                    activeDialog = task.dialogBuilder(nextFunc)
                    activeDialog?.show()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
                next.receive()
            }
        }

        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                activeDialog?.dismiss()
                activeDialog = null
            }
        })
    }

    fun offer(tag: String, priority: Int, dialogBuilder: (next: () -> Unit) -> Dialog) {
        val task = Task(tag, priority, dialogBuilder)
        if (pq.contains(task)) {
            return
        }
        pq.offer(task)
        queue.trySend(Unit)
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