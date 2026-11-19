package com.example.tracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.tracker.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditExpenseActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_expense)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = AppDatabase.getInstance(this)

        val expenseId = intent.getIntExtra(EXTRA_ID, 0)
        val etTitle = findViewById<EditText>(R.id.et_title)
        val etAmount = findViewById<EditText>(R.id.et_amount)
        val btnUpdate = findViewById<Button>(R.id.btn_update)
        val btnDelete = findViewById<Button>(R.id.btn_delete)

        if (expenseId == 0) {
            Toast.makeText(this, "Expense not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        lifecycleScope.launch {
            val expense = withContext(Dispatchers.IO) { db.appDao().getExpenseById(expenseId) }
            if (expense == null) {
                Toast.makeText(this@EditExpenseActivity, "Expense not found", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            etTitle.setText(expense.title)
            etAmount.setText(expense.amount.toString())

            btnUpdate.setOnClickListener {
                val title = etTitle.text.toString().trim()
                val amount = etAmount.text.toString().trim().replace(',', '.').toDoubleOrNull()

                if (title.isEmpty()) {
                    Toast.makeText(this@EditExpenseActivity, "Enter a title", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (amount == null || amount <= 0.0) {
                    Toast.makeText(this@EditExpenseActivity, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    // copy بيحافظ على id و userId و categoryId
                    withContext(Dispatchers.IO) {
                        db.appDao().updateExpense(expense.copy(title = title, amount = amount))
                    }
                    Toast.makeText(this@EditExpenseActivity, "Updated!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            btnDelete.setOnClickListener {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { db.appDao().deleteExpense(expense) }
                    Toast.makeText(this@EditExpenseActivity, "Deleted!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    companion object {
        const val EXTRA_ID = "EXTRA_ID"
    }
}
