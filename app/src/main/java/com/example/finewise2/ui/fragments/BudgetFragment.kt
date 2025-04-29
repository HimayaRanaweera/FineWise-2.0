package com.example.finewise2.ui.fragments

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.finewise2.R
import com.example.finewise2.viewmodel.TransactionViewModel
import java.util.*

class BudgetFragment : Fragment() {
    companion object {
        var notificationSentThisSession = false
    }

    private lateinit var currencySpinner: Spinner
    private lateinit var budgetAmountEdit: EditText
    private lateinit var saveBudgetBtn: Button
    private lateinit var textBudget: TextView
    private lateinit var textTotalIncome: TextView
    private lateinit var textTotalExpenses: TextView
    private lateinit var textRemaining: TextView
    private lateinit var textNetSavings: TextView
    private lateinit var progressBudgetUsage: ProgressBar
    private lateinit var textSavingsRate: TextView
    private lateinit var transactionViewModel: TransactionViewModel

    private var selectedCurrency = "USD"
    private var budgetAmount = 0.0
    private val currencyList = arrayOf("USD", "EUR", "INR", "GBP", "JPY")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_budget, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currencySpinner = view.findViewById(R.id.spinnerCurrency)
        budgetAmountEdit = view.findViewById(R.id.editTextBudgetAmount)
        saveBudgetBtn = view.findViewById(R.id.btnSaveBudget)
        textBudget = view.findViewById(R.id.textBudget)
        textTotalIncome = view.findViewById(R.id.textTotalIncome)
        textTotalExpenses = view.findViewById(R.id.textTotalExpenses)
        textRemaining = view.findViewById(R.id.textRemaining)
        textNetSavings = view.findViewById(R.id.textNetSavings)
        progressBudgetUsage = view.findViewById(R.id.progressBudgetUsage)
        textSavingsRate = view.findViewById(R.id.textSavingsRate)

        // Currency spinner setup
        val currencyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencyList)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner.adapter = currencyAdapter
        currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCurrency = currencyList[position]
                updateBudgetStatus()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Load saved budget and currency
        val prefs = requireContext().getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)
        selectedCurrency = prefs.getString("currency", "USD") ?: "USD"
        budgetAmount = prefs.getFloat("budget", 0f).toDouble()
        currencySpinner.setSelection(currencyList.indexOf(selectedCurrency))
        if (budgetAmount > 0) budgetAmountEdit.setText(budgetAmount.toString())

        saveBudgetBtn.setOnClickListener {
            val amount = budgetAmountEdit.text.toString().toDoubleOrNull() ?: 0.0
            budgetAmount = amount
            prefs.edit().putString("currency", selectedCurrency).putFloat("budget", amount.toFloat()).apply()
            updateBudgetStatus()
            Toast.makeText(requireContext(), "Budget saved!", Toast.LENGTH_SHORT).show()
            // Check if budget is exceeded and send notification
            val transactions = transactionViewModel.allTransactions.value ?: emptyList()
            val totalExpenses = transactions.filter { it.type.name == "EXPENSE" }.sumOf { it.amount }
            if (budgetAmount > 0 && totalExpenses > budgetAmount) {
                sendBudgetExceededNotification(requireContext(), selectedCurrency, budgetAmount, totalExpenses)
            }
        }

        transactionViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(TransactionViewModel::class.java)
        transactionViewModel.allTransactions.observe(viewLifecycleOwner) {
            updateBudgetStatus()
        }

        // Reset notification flag if needed (e.g., on app restart)
        if (savedInstanceState == null) notificationSentThisSession = false

        updateBudgetStatus()
    }

    private fun updateBudgetStatus() {
        val context = requireContext()
        val prefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)
        val budget = prefs.getFloat("budget", 0f).toDouble()
        val currency = prefs.getString("currency", "USD") ?: "USD"
        val transactions = transactionViewModel.allTransactions.value ?: emptyList()
        val totalIncome = transactions.filter { it.type.name == "INCOME" }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type.name == "EXPENSE" }.sumOf { it.amount }
        val remaining = budget - totalExpenses
        val netSavings = totalIncome - totalExpenses
        val usage = if (budget > 0) (totalExpenses / budget * 100).toInt().coerceAtMost(100) else 0
        val savingsRate = if (totalIncome > 0) (netSavings / totalIncome * 100) else 0.0

        textBudget.text = "Budget: $currency${"%.2f".format(budget)}"
        textTotalIncome.text = "Total Income: $currency${"%.2f".format(totalIncome)}"
        textTotalExpenses.text = "Total Expenses: $currency${"%.2f".format(totalExpenses)}"
        textRemaining.text = "Remaining: $currency${"%.2f".format(remaining)}"
        textNetSavings.text = "Net Savings: $currency${"%.2f".format(netSavings)}"
        progressBudgetUsage.progress = usage
        textSavingsRate.text = "Savings Rate: ${"%.1f".format(savingsRate)}%"

        // Color remaining and net savings
        textRemaining.setTextColor(if (remaining >= 0) 0xFF388E3C.toInt() else 0xFFD32F2F.toInt())
        textNetSavings.setTextColor(if (netSavings >= 0) 0xFF388E3C.toInt() else 0xFFD32F2F.toInt())

        // Push notification if budget exceeded, only once per session
        if (budget > 0 && totalExpenses > budget && !notificationSentThisSession) {
            sendBudgetExceededNotification(context, currency, budget, totalExpenses)
            notificationSentThisSession = true
        }
    }


    private fun sendBudgetExceededNotification(context: Context, currency: String, budget: Double, expenses: Double) {
        val channelId = "budget_alerts"
        val notificationId = 1001

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Budget Alerts", NotificationManager.IMPORTANCE_HIGH)
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Budget Exceeded!")
            .setContentText("You have exceeded your budget: $currency${"%.2f".format(expenses)} > $currency${"%.2f".format(budget)}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // ✅ Check for permission before sending
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }

}