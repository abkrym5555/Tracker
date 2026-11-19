package com.example.tracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import com.example.tracker.data.SessionPrefs
import com.example.tracker.ui.auth.AuthRoute
import com.example.tracker.ui.auth.AuthViewModel
import com.example.tracker.ui.theme.ExpenseTrackerTheme


class AuthActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        if (SessionPrefs(this).isLoggedIn) {
            navigateToMain()
            return
        }

        setContent {
            ExpenseTrackerTheme {
                AuthRoute(
                    viewModel = viewModel,
                    onLoggedIn = ::navigateToMain
                )
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
