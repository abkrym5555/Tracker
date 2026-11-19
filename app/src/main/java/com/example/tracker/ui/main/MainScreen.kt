package com.example.tracker.ui.main

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tracker.R
import com.example.tracker.data.Expense
import com.example.tracker.ui.common.AppTextField
import com.example.tracker.ui.common.money
import com.example.tracker.ui.theme.AppColors
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun MainRoute(
    viewModel: MainViewModel,
    initialExpenseTitle: String,
    onOpenExpense: (Int) -> Unit,
    onShareSummary: (String) -> Unit,
    onRequireAuth: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()


    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }


    LaunchedEffect(state.mustReauthenticate) {
        if (state.mustReauthenticate) onRequireAuth()
    }

    MainScreen(
        state = state,
        initialExpenseTitle = initialExpenseTitle,
        onSaveBudget = viewModel::saveBudget,
        onAddExpense = viewModel::addExpense,
        onOpenExpense = { onOpenExpense(it.id) },
        onShare = { onShareSummary(viewModel.summaryText()) },
        onLogout = viewModel::logout
    )
}

@Composable
fun MainScreen(
    state: MainUiState,
    initialExpenseTitle: String,
    onSaveBudget: (String) -> String?,
    onAddExpense: (String, String) -> String?,
    onOpenExpense: (Expense) -> Unit,
    onShare: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var budgetInput by rememberSaveable { mutableStateOf("") }
    var expenseTitle by rememberSaveable { mutableStateOf(initialExpenseTitle) }
    var expenseAmount by rememberSaveable { mutableStateOf("") }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    val toast: (String) -> Unit = { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgScreen)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // ===== الهيدر =====
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(AppColors.NavyDeep),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.username.take(1).uppercase(Locale.US),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Expense Tracker",
                modifier = Modifier.weight(1f),
                color = AppColors.Slate900,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            ActionIcon(R.drawable.ic_share, "Share", AppColors.Indigo500, onShare)
            Spacer(Modifier.width(14.dp))
            ActionIcon(R.drawable.ic_logout, "Logout", AppColors.Red500) {
                showLogoutDialog = true
            }
        }

        Text(
            text = "Hi, ${state.username}",
            modifier = Modifier.padding(top = 6.dp),
            color = AppColors.Slate500,
            fontSize = 14.sp
        )


        Spacer(Modifier.height(18.dp))
        SummaryCard(state)


        AnimatedVisibility(
            visible = state.isOverBudget,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            OverBudgetNotice(
                overBy = money(state.totalSpent - state.budget),
                modifier = Modifier.padding(top = 10.dp)
            )
        }


        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTextField(
                value = budgetInput,
                onValueChange = { budgetInput = it },
                placeholder = "Set New Budget",
                modifier = Modifier.weight(1f),
                horizontalPadding = 16.dp,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = {
                    val error = onSaveBudget(budgetInput)
                    if (error != null) {
                        toast(error)
                    } else {
                        budgetInput = ""
                        focusManager.clearFocus()
                        keyboard?.hide()
                        toast("Budget saved")
                    }
                },
                modifier = Modifier
                    .width(96.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Indigo500,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text("Save", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }


        Spacer(Modifier.height(14.dp))
        QuickAddCard(
            title = expenseTitle,
            amount = expenseAmount,
            onTitleChange = { expenseTitle = it },
            onAmountChange = { expenseAmount = it },
            onSubmit = {
                val error = onAddExpense(expenseTitle, expenseAmount)
                if (error != null) {
                    toast(error)
                } else {
                    expenseTitle = ""
                    expenseAmount = ""
                    focusManager.clearFocus()
                    keyboard?.hide()
                }
            }
        )


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transactions",
                modifier = Modifier.weight(1f),
                color = AppColors.Slate900,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (state.expenses.size == 1) "1 item" else "${state.expenses.size} items",
                color = AppColors.Slate400,
                fontSize = 12.sp
            )
        }

        when {
            state.isLoading && state.expenses.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(26.dp),
                        color = AppColors.Indigo500,
                        strokeWidth = 3.dp
                    )
                }
            }

            state.expenses.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions yet",
                        color = AppColors.Slate400,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                state.expenses.forEach { expense ->
                    ExpenseRow(expense = expense, onClick = { onOpenExpense(expense) })
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out?") },
            text = { Text("You can log in again anytime.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Log out", color = AppColors.Red600)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
private fun ActionIcon(
    @DrawableRes icon: Int,
    description: String,
    tint: Color,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(icon),
        contentDescription = description,
        colorFilter = ColorFilter.tint(tint),
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(5.dp)
    )
}

@Composable
private fun SummaryCard(state: MainUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(AppColors.IndigoCardStart, AppColors.IndigoCardEnd)
                )
            )
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Total Spent",
                modifier = Modifier.weight(1f),
                color = AppColors.TextOnCard,
                fontSize = 13.sp
            )
            Text(
                text = "Budget: " + money(state.budget),
                color = AppColors.TextOnCard,
                fontSize = 13.sp
            )
        }

        Text(
            text = money(state.totalSpent),
            modifier = Modifier.padding(top = 6.dp),
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(18.dp))
        BudgetBar(
            fraction = state.progressFraction,
            isOverBudget = state.isOverBudget
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.budget > 0.0) {
                    "Remaining: " + money(state.remaining)
                } else {
                    "No budget set"
                },
                modifier = Modifier.weight(1f),
                color = AppColors.TextOnCard,
                fontSize = 13.sp
            )
            Text(
                text = "${state.percent.roundToInt()}%",
                color = AppColors.TextOnCard,
                fontSize = 13.sp
            )
        }
    }
}


@Composable
private fun BudgetBar(fraction: Float, isOverBudget: Boolean) {
    val barColor = if (isOverBudget) AppColors.Red500 else AppColors.Green500

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White.copy(alpha = 0.30f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .background(barColor)
        )
    }
}


@Composable
private fun OverBudgetNotice(overBy: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Red100)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_warning),
            contentDescription = "Warning",
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "Over budget by $overBy",
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            color = AppColors.Red600,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun QuickAddCard(
    title: String,
    amount: String,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, AppColors.CardStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quick Add Expense",
                color = AppColors.Slate900,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    placeholder = "Title",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                AppTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    placeholder = "Amount",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Indigo500,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_plus),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Add Transaction", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
private fun ExpenseRow(expense: Expense, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, AppColors.CardStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AppColors.IndigoSoft),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_cart),
                    contentDescription = "Category",
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = expense.title,
                    color = AppColors.Slate900,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Tap to edit",
                    modifier = Modifier.padding(top = 2.dp),
                    color = AppColors.Slate400,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "-" + money(expense.amount),
                modifier = Modifier.padding(start = 10.dp),
                color = AppColors.Red500,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
