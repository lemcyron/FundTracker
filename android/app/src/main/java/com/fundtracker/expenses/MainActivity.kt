package com.fundtracker.expenses

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.fundtracker.expenses.ui.screens.ExpenseTrackerScreen
import com.fundtracker.expenses.ui.theme.FundTrackerTheme
import com.fundtracker.expenses.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FundTrackerTheme {
                ExpenseTrackerScreen(viewModel = viewModel)
            }
        }
    }
}
