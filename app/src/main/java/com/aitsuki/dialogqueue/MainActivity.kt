package com.aitsuki.dialogqueue

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aitsuki.dialogqueue.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val adapter = DialogSampleAdapter()

    private val dialogQueue = DialogQueue(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        binding.addBtn.setOnClickListener {
            showEditDialog()
        }
        binding.showBtn.setOnClickListener {
            val dataList = adapter.getDataList()
            for ((tag, priority) in dataList) {
                dialogQueue.offer(tag, priority) { next ->
                    MaterialAlertDialogBuilder(this)
                        .setTitle("$tag - $priority")
                        .setMessage(getString(R.string.dialog_message, priority))
                        .setPositiveButton(getString(R.string.get_it), null)
                        .setOnDismissListener { next() }
                        .create()
                }
            }
        }
    }

    private fun showEditDialog() {
        val editText = EditText(this)
        editText.hint = getString(R.string.please_enter)

        val container = FrameLayout(this)
        container.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        val padding = (24 * resources.displayMetrics.density).roundToInt()
        container.updatePadding(left = padding, top = padding, right = padding)
        container.addView(editText)

        MaterialAlertDialogBuilder(this)
            .setView(container)
            .setTitle(getString(R.string.add_new_dialog))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                val title = editText.text.trim().toString()
                if (title.isNotBlank()) {
                    adapter.addData(title)
                }
            }
            .show()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                onChanged()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                onChanged()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                onChanged()
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                onChanged()
            }

            override fun onChanged() {
                val dataList = adapter.getDataList().distinctBy { it.first }
                if (dataList.size < 2) {
                    binding.expectOrder.text = ""
                    binding.actualOrder.text = ""
                    return
                }

                val expectList = dataList.sortedBy { it.second }
                val expectListStr = expectList.joinToString(", ") { it.first }
                binding.expectOrder.text = getString(R.string.expect_order, expectListStr)

                val actualListStr = "${dataList.first().first}, " +
                        dataList.subList(1, dataList.size)
                            .sortedBy { it.second }
                            .joinToString(", ") { it.first }

                binding.actualOrder.text = getString(R.string.actual_order, actualListStr)

            }
        })

        adapter.setDataList(
            listOf(
                "Fourth" to 4,
                "Third" to 3,
                "Second" to 2,
                "First" to 1,
            )
        )
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                adapter.move(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (viewHolder != null && actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder.itemView.setBackgroundColor(Color.WHITE)
                }
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.background = null
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }
}