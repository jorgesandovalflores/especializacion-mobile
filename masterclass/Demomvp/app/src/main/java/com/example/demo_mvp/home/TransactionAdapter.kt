package com.example.demo_mvp.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.demo_mvp.databinding.ItemTransactionBinding

class TransactionAdapter(
    private val onItemClicked: (Transaction) -> Unit,
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    private val items = mutableListOf<Transaction>()

    fun submitList(transactions: List<Transaction>) {
        items.clear()
        items.addAll(transactions)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onItemClicked)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction, onItemClicked: (Transaction) -> Unit) {
            binding.transactionRow.setBackgroundResource(transaction.rowBackgroundRes)
            binding.badge.background?.mutate()?.setTint(transaction.badgeColor)
            binding.badgeIcon.setImageResource(transaction.iconRes)
            binding.tvTitle.text = transaction.title
            binding.tvCategory.text = transaction.category
            binding.tvAmount.text = transaction.amount
            binding.tvTime.text = transaction.time
            binding.transactionRow.setOnClickListener { onItemClicked(transaction) }
        }
    }
}
