package com.example.tracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import com.example.tracker.data.SessionPrefs
import com.example.tracker.ui.main.MainRoute
import com.example.tracker.ui.main.MainViewModel
import com.example.tracker.ui.theme.ExpenseTrackerTheme

/**
 * الشاشة الرئيسية بقت Compose بالكامل.
 * الـ Activity دورها بس: الجلسة + الانتقالات + الـ Share + إنشاء الـ ViewModel.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // الشاشة فاتحة، فأيقونات شريط الحالة لازم تكون غامقة
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        // لو الشاشة اتفتحت من غير تسجيل دخول (مثلاً من شير خارجي) نرجّعه للـ login
        if (SessionPrefs(this).username.isEmpty()) {
            goToAuth()
            return
        }

        // لو التطبيق اتفتح من شير نص، نحط النص في خانة العنوان
        val sharedTitle = if (savedInstanceState == null && intent?.action == Intent.ACTION_SEND) {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()?.take(60).orEmpty()
        } else {
            ""
        }

        setContent {
            ExpenseTrackerTheme {
                MainRoute(
                    viewModel = viewModel,
                    initialExpenseTitle = sharedTitle,
                    onOpenExpense = ::openExpense,
                    onShareSummary = ::shareSummary,
                    onRequireAuth = ::goToAuth
                )
            }
        }
    }

    private fun openExpense(expenseId: Int) {
        startActivity(
            Intent(this, EditExpenseActivity::class.java).apply {
                putExtra(EditExpenseActivity.EXTRA_ID, expenseId)
            }
        )
    }

    private fun shareSummary(text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Expense Tracker summary")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(send, "Share summary"))
    }

    private fun goToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
