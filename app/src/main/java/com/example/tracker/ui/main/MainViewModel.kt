package com.example.tracker.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.AppDatabase
import com.example.tracker.data.Category
import com.example.tracker.data.Expense
import com.example.tracker.data.SessionPrefs
import com.example.tracker.ui.common.money
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * حالة الشاشة الرئيسية مرة واحدة، وكل الأرقام المشتقة محسوبة هنا
 * بدل ما تتوزع على الـ Activity.
 */
data class MainUiState(
    val username: String = "",
    val budget: Double = 0.0,
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = true,
    val mustReauthenticate: Boolean = false
) {
    val totalSpent: Double get() = expenses.sumOf { it.amount }
    val remaining: Double get() = budget - totalSpent

    /** بنعتبره تعدّي بس لو فيه ميزانية متحددة. */
    val isOverBudget: Boolean get() = budget > 0.0 && totalSpent > budget

    /** النسبة الحقيقية — ممكن تعدّي 100%. */
    val percent: Double get() = if (budget > 0.0) (totalSpent / budget) * 100.0 else 0.0

    /** الشريط بيتقفل عند 100% عشان ميزيدش عن طوله. */
    val progressFraction: Float get() = (percent / 100.0).coerceIn(0.0, 1.0).toFloat()
}

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)
    private val session = SessionPrefs(app)

    private val _state = MutableStateFlow(
        MainUiState(username = session.username, budget = session.budgetFor())
    )
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    private var userId: Int = session.userId

    /**
     * بتتنادى كل مرة الشاشة بترجع للـ resume، عشان أي تعديل/حذف في
     * شاشة EditExpenseActivity يظهر هنا على طول.
     */
    fun refresh() {
        val username = session.username
        if (username.isEmpty()) {
            _state.update { it.copy(mustReauthenticate = true, isLoading = false) }
            return
        }

        viewModelScope.launch {
            val uid = resolveUserId(username)
            if (uid == 0) {
                _state.update { it.copy(mustReauthenticate = true, isLoading = false) }
                return@launch
            }

            val expenses = withContext(Dispatchers.IO) {
                ensureDefaultCategory()
                db.appDao().getExpensesForUser(uid)
            }

            _state.update {
                it.copy(
                    username = username,
                    budget = session.budgetFor(username),
                    expenses = expenses,
                    isLoading = false
                )
            }
        }
    }

    /** بيرجّع رسالة خطأ للواجهة، أو null لو الحفظ نجح. */
    fun saveBudget(rawValue: String): String? {
        val value = rawValue.trim().replace(',', '.').toDoubleOrNull()
        if (value == null || value <= 0.0) return "Enter a budget greater than 0"

        session.saveBudget(value)
        _state.update { it.copy(budget = value) }
        return null
    }

    /** بيرجّع رسالة خطأ للواجهة، أو null لو الإضافة نجحت. */
    fun addExpense(title: String, rawAmount: String): String? {
        val cleanTitle = title.trim()
        val amount = rawAmount.trim().replace(',', '.').toDoubleOrNull()

        if (cleanTitle.isEmpty()) return "Enter a title"
        if (amount == null || amount <= 0.0) return "Enter a valid amount"

        viewModelScope.launch {
            val uid = resolveUserId(session.username)
            if (uid == 0) {
                _state.update { it.copy(mustReauthenticate = true) }
                return@launch
            }
            withContext(Dispatchers.IO) {
                val categoryId = ensureDefaultCategory()
                db.appDao().insertExpense(
                    Expense(
                        userId = uid,
                        categoryId = categoryId,
                        title = cleanTitle,
                        amount = amount
                    )
                )
            }
            refresh()
        }
        return null
    }

    fun logout() {
        // بنشيل بيانات الدخول بس، والبيانات والميزانية بتفضل محفوظة
        session.clearSession()
        _state.update { it.copy(mustReauthenticate = true) }
    }

    fun summaryText(): String {
        val s = _state.value
        return buildString {
            appendLine("Expense Tracker summary")
            appendLine("Budget: ${money(s.budget)}")
            appendLine("Total spent: ${money(s.totalSpent)}")
            appendLine("Remaining: ${money(s.remaining)}")
            if (s.isOverBudget) appendLine("Over budget by: ${money(s.totalSpent - s.budget)}")
            append("Transactions: ${s.expenses.size}")
        }
    }

    /**
     * بيرجّع id المستخدم الحالي. لو مش محفوظ في الـ prefs (تحديث من نسخة قديمة)
     * بنجيبه من قاعدة البيانات بالاسم ونحفظه، عشان الجلسة متتكسرش.
     */
    private suspend fun resolveUserId(username: String): Int {
        if (userId != 0) return userId
        val found = withContext(Dispatchers.IO) {
            db.appDao().getUserByUsername(username)?.id ?: 0
        }
        if (found != 0) {
            userId = found
            session.saveUserId(found)
        }
        return found
    }

    /**
     * جدول المصروفات مربوط بجدول التصنيفات بمفتاح أجنبي،
     * فمحتاجين تصنيف واحد على الأقل قبل أي إضافة.
     */
    private suspend fun ensureDefaultCategory(): Int {
        val categories = db.appDao().getAllCategories()
        return categories.firstOrNull()?.id
            ?: db.appDao().insertCategory(Category(name = "General")).toInt()
    }
}
