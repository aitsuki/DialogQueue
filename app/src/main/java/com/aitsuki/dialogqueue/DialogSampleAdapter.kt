package com.aitsuki.dialogqueue

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aitsuki.dialogqueue.databinding.ItemDialogSampleBinding
import java.util.Collections

class DialogSampleAdapter : RecyclerView.Adapter<DialogSampleAdapter.DialogSampleViewHolder>() {

    private var items = arrayListOf<Pair<String, Int>>()

    @SuppressLint("NotifyDataSetChanged")
    fun setDataList(list: List<Pair<String, Int>>) {
        items = ArrayList(list)
        notifyDataSetChanged()
    }

    fun getDataList(): List<Pair<String, Int>> {
        return items
    }

    fun addData(item: String) {
        val maxPriority = items.maxByOrNull { it.second }?.second ?: 0
        items.add(item to maxPriority + 1)
        notifyItemInserted(items.size - 1)
    }

    fun removeData(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun move(from: Int, to: Int) {
        Collections.swap(items, from, to)
        notifyItemMoved(from, to)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogSampleViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDialogSampleBinding.inflate(inflater, parent, false)
        return DialogSampleViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: DialogSampleViewHolder, position: Int) {
        val binding = holder.binding
        binding.title.text = items[position].first
        binding.priority.text = items[position].second.toString()
        binding.deleteBtn.setOnClickListener {
            removeData(holder.adapterPosition)
        }
    }

    class DialogSampleViewHolder(val binding: ItemDialogSampleBinding) :
        RecyclerView.ViewHolder(binding.root)
}

