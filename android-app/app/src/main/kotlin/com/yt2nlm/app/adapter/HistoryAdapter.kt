package com.yt2nlm.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yt2nlm.app.databinding.ItemHistoryBinding
import com.yt2nlm.app.model.HistoryItem
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter(
    private val onItemClick: (HistoryItem) -> Unit,
    private val onDeleteClick: (HistoryItem) -> Unit
) : ListAdapter<HistoryItem, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {
    
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return HistoryViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(historyItem: HistoryItem) {
            binding.apply {
                // URL表示
                urlText.text = historyItem.getDisplayUrl()
                
                // 日付表示
                dateText.text = dateFormat.format(historyItem.lastUsed)
                
                // 使用回数が1回以上の場合は表示
                if (historyItem.successCount > 1) {
                    dateText.text = "${dateText.text} (${historyItem.successCount}回)"
                }
                
                // クリックイベント
                root.setOnClickListener {
                    onItemClick(historyItem)
                }
                
                // 再開ボタン
                reopenButton.setOnClickListener {
                    onItemClick(historyItem)
                }
                
                // 削除ボタン
                deleteButton.setOnClickListener {
                    onDeleteClick(historyItem)
                }
            }
        }
    }
}

class HistoryDiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
    override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
        return oldItem == newItem
    }
}