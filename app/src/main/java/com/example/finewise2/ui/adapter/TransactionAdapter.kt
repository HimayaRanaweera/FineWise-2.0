package com.example.finewise2.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finewise2.R
import com.example.finewise2.data.model.Transaction
import com.example.finewise2.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val listener: OnTransactionActionListener
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    interface OnTransactionActionListener {
        fun onEdit(transaction: Transaction)
        fun onDelete(transaction: Transaction)
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.textTitle)
        val amount: TextView = itemView.findViewById(R.id.textAmount)
        val category: TextView = itemView.findViewById(R.id.textCategory)
        val date: TextView = itemView.findViewById(R.id.textDate)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.title.text = transaction.title
        holder.amount.text = String.format(Locale.US, "%.2f", transaction.amount)
        holder.category.text = transaction.category
        val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.US)
        holder.date.text = dateFormat.format(transaction.date)
        if (transaction.type == TransactionType.INCOME) {
            holder.amount.setTextColor(Color.parseColor("#388E3C")) // Green
        } else {
            holder.amount.setTextColor(Color.parseColor("#D32F2F")) // Red
        }
        holder.btnEdit.setOnClickListener { listener.onEdit(transaction) }
        holder.btnDelete.setOnClickListener { listener.onDelete(transaction) }
    }

    override fun getItemCount(): Int = transactions.size

    fun setTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}