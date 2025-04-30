package com.example.finewise2.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finewise2.R
import com.example.finewise2.data.model.Transaction
import com.example.finewise2.data.model.TransactionType
import com.example.finewise2.ui.adapter.TransactionAdapter
import com.example.finewise2.viewmodel.TransactionViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.ArrayAdapter
import android.text.format.DateFormat
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*

class TransactionFragment : Fragment() {
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var adapter: TransactionAdapter
    private var editingTransaction: Transaction? = null
    private val gson = Gson()
    private val backupFileName = "transactions_backup.json"
    private val textFileName = "transactions_export.txt"
    private var lastDeletedTransaction: Transaction? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // No-op, handled in button click
        }

    private val restoreFilePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { importTransactionsFromUri(it) }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.transactionRecyclerView)
        adapter = TransactionAdapter(emptyList(), object : TransactionAdapter.OnTransactionActionListener {
            override fun onEdit(transaction: Transaction) {
                editingTransaction = transaction
                showAddTransactionDialog(transaction)
            }
            override fun onDelete(transaction: Transaction) {
                lastDeletedTransaction = transaction.copy(id = 0) // Reset id for Room auto-generate
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Delete") { _, _ ->
                        transactionViewModel.delete(transaction)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        transactionViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(TransactionViewModel::class.java)
        transactionViewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            adapter.setTransactions(transactions.sortedBy { it.date })
        }

        view.findViewById<FloatingActionButton>(R.id.fab_add_transaction).setOnClickListener {
            showAddTransactionDialog()
        }

        view.findViewById<View>(R.id.btnBackup).setOnClickListener { backupTransactions() }
        view.findViewById<View>(R.id.btnRestore).setOnClickListener {
            lastDeletedTransaction?.let {
                transactionViewModel.insert(it)
                Toast.makeText(requireContext(), "Transaction restored!", Toast.LENGTH_SHORT).show()
                lastDeletedTransaction = null
            } ?: Toast.makeText(requireContext(), "No transaction to restore!", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btnExportText).setOnClickListener { exportTransactionsAsText() }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            }
        }
    }

    private fun showAddTransactionDialog(transaction: Transaction? = null) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_transaction, null)
        val titleEdit = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val amountEdit = dialogView.findViewById<EditText>(R.id.editTextAmount)
        val categorySpinner = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerCategory)
        val dateEdit = dialogView.findViewById<EditText>(R.id.editTextDate)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroupType)
        val radioIncome = dialogView.findViewById<RadioButton>(R.id.radioIncome)
        val radioExpense = dialogView.findViewById<RadioButton>(R.id.radioExpense)
        val isEdit = transaction != null
        if (isEdit) {
            titleEdit.setText(transaction!!.title)
            amountEdit.setText(transaction.amount.toString())
            dateEdit.setText(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(transaction.date))
            if (transaction.type == com.example.finewise2.data.model.TransactionType.INCOME) radioIncome.isChecked = true else radioExpense.isChecked = true
        } else {
            radioIncome.isChecked = true
            val today = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            dateEdit.setText(dateFormat.format(today))
        }
        fun setCategoryAdapter(isIncome: Boolean) {
            val arrayRes = if (isIncome) R.array.income_categories else R.array.expense_categories
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resources.getStringArray(arrayRes))
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
            if (isEdit) {
                val categories = resources.getStringArray(arrayRes)
                val idx = categories.indexOf(transaction?.category)
                if (idx >= 0) categorySpinner.setSelection(idx)
            }
        }
        setCategoryAdapter(isEdit && transaction!!.type == com.example.finewise2.data.model.TransactionType.INCOME || !isEdit)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            setCategoryAdapter(checkedId == R.id.radioIncome)
        }
        AlertDialog.Builder(requireContext())
            .setTitle(if (isEdit) "Edit Transaction" else "Add Transaction")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = titleEdit.text.toString()
                val amountText = amountEdit.text.toString()
                val dateStr = dateEdit.text.toString()

                if (title.isBlank()) {
                    Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val amount = amountText.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    Toast.makeText(requireContext(), "Enter a valid positive amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val date = try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr) ?: throw Exception("Invalid date")
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Invalid date format. Use yyyy-MM-dd", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val category = categorySpinner.selectedItem?.toString() ?: "Other"
                val type = if (radioGroup.checkedRadioButtonId == R.id.radioIncome)
                    TransactionType.INCOME else TransactionType.EXPENSE

                val newTransaction = if (isEdit) {
                    transaction!!.copy(title = title, amount = amount, category = category, date = date, type = type)
                } else {
                    Transaction(title = title, amount = amount, category = category, date = date, type = type)
                }

                if (isEdit) transactionViewModel.update(newTransaction)
                else transactionViewModel.insert(newTransaction)
                editingTransaction = null
            }

            .setNegativeButton("Cancel") { _, _ -> editingTransaction = null }
            .show()
    }

    private fun checkStoragePermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return true
        val write = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val read = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        if (write != PackageManager.PERMISSION_GRANTED || read != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ))
            return false
        }
        return true
    }

    private fun backupTransactions() {
        if (!checkStoragePermissions()) return
        val transactions = transactionViewModel.allTransactions.value ?: emptyList()
        val json = gson.toJson(transactions)
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), backupFileName)
        try {
            file.writeText(json)
            Toast.makeText(requireContext(), "Backup saved to Downloads", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun restoreTransactions() {
        if (!checkStoragePermissions()) return
        restoreFilePicker.launch("application/json")
    }

    private fun importTransactionsFromUri(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val json = inputStream?.bufferedReader().use { it?.readText() } ?: return
            val type = object : TypeToken<List<Transaction>>() {}.type
            val transactions: List<Transaction> = gson.fromJson(json, type)
            for (transaction in transactions) {
                transactionViewModel.insert(transaction)
            }
            Toast.makeText(requireContext(), "Transactions restored!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Restore failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun exportTransactionsAsText() {
        if (!checkStoragePermissions()) return
        val transactions = transactionViewModel.allTransactions.value ?: emptyList()
        val sb = StringBuilder()
        for (t in transactions) {
            sb.append("Title: ${t.title}\nAmount: ${t.amount}\nCategory: ${t.category}\nDate: ${t.date}\nType: ${t.type}\n\n")
        }
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), textFileName)
        try {
            file.writeText(sb.toString())
            Toast.makeText(requireContext(), "Exported as text to Downloads", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}