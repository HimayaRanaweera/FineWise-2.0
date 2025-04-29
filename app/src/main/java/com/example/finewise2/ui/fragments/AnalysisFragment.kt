package com.example.finewise2.ui.fragments

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.finewise2.R
import com.example.finewise2.viewmodel.TransactionViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class AnalysisFragment : Fragment() {
    private lateinit var textTotalIncome: TextView
    private lateinit var textTotalExpenses: TextView
    private lateinit var textBalance: TextView
    private lateinit var pieChartIncome: PieChart
    private lateinit var pieChartExpense: PieChart
    private lateinit var layoutExpenseCategories: LinearLayout
    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analysis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textTotalIncome = view.findViewById(R.id.textTotalIncome)
        textTotalExpenses = view.findViewById(R.id.textTotalExpenses)
        textBalance = view.findViewById(R.id.textBalance)
        pieChartIncome = view.findViewById(R.id.pieChartIncome)
        pieChartExpense = view.findViewById(R.id.pieChartExpense)
        layoutExpenseCategories = view.findViewById(R.id.layoutExpenseCategories)

        transactionViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(TransactionViewModel::class.java)
        transactionViewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            val prefs = requireContext().getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)
            val currency = prefs.getString("currency", "USD") ?: "USD"
            val budget = prefs.getFloat("budget", 0f).toDouble()
            val totalIncome = transactions.filter { it.type.name == "INCOME" }.sumOf { it.amount }
            val totalExpenses = transactions.filter { it.type.name == "EXPENSE" }.sumOf { it.amount }
            val balance = totalIncome - totalExpenses
            textTotalIncome.text = "Total Income: $currency${"%.2f".format(totalIncome)}"
            textTotalExpenses.text = "Total Expenses: $currency${"%.2f".format(totalExpenses)}"
            textBalance.text = "Balance: $currency${"%.2f".format(balance)}"

            // Pie chart for income categories
            val incomeMap = transactions.filter { it.type.name == "INCOME" }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
            setPieChart(pieChartIncome, incomeMap, "Income", true, currency)

            // Pie chart for expense categories
            val expenseMap = transactions.filter { it.type.name == "EXPENSE" }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
            setPieChart(pieChartExpense, expenseMap, "Expense", false, currency)

            // Expense categories summary
            layoutExpenseCategories.removeAllViews()
            for ((category, amount) in expenseMap) {
                val count = transactions.count { it.type.name == "EXPENSE" && it.category == category }
                val summaryView = LayoutInflater.from(requireContext()).inflate(R.layout.item_expense_category_summary, layoutExpenseCategories, false)
                summaryView.findViewById<TextView>(R.id.textCategoryName).text = category
                summaryView.findViewById<TextView>(R.id.textCategoryAmount).text = "$currency${"%.2f".format(amount)}"
                summaryView.findViewById<TextView>(R.id.textCategoryCount).text = "$count transactions"
                layoutExpenseCategories.addView(summaryView)
            }
        }
    }

    private fun setPieChart(pieChart: PieChart, dataMap: Map<String, Double>, label: String, isIncome: Boolean, currency: String) {
        val entries = dataMap.map { PieEntry(it.value.toFloat(), it.key) }
        val dataSet = PieDataSet(entries, label)
        dataSet.colors = getChartColors(entries.size, isIncome)
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.BLACK
        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(false)
        pieChart.setDrawEntryLabels(false)
        pieChart.setDrawCenterText(true)
        val total = entries.sumOf { it.value.toDouble() }
        pieChart.centerText = if (entries.isNotEmpty()) "$currency${"%.2f".format(total)}\n$label" else "No data"
        pieChart.setCenterTextSize(14f)
        pieChart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
        pieChart.legend.isEnabled = true
        pieChart.legend.textSize = 12f
        pieChart.legend.form = Legend.LegendForm.SQUARE
        pieChart.invalidate()
    }

    private fun getChartColors(size: Int, isIncome: Boolean): List<Int> {
        val colors = if (isIncome) {
            listOf(
                Color.rgb(76, 175, 80),   // Green
                Color.rgb(129, 199, 132), // Light Green
                Color.rgb(165, 214, 167), // Lighter Green
                Color.rgb(200, 230, 201), // Very Light Green
                Color.rgb(232, 245, 233)  // Lightest Green
            )
        } else {
            listOf(
                Color.rgb(244, 67, 54),   // Red
                Color.rgb(239, 154, 154), // Light Red
                Color.rgb(229, 115, 115), // Lighter Red
                Color.rgb(239, 83, 80),   // Very Light Red
                Color.rgb(255, 205, 210)  // Lightest Red
            )
        }
        return if (size <= colors.size) colors.take(size) else List(size) { colors[it % colors.size] }
    }
}