package com.fundtracker.expenses.data

import android.content.Context
import android.content.SharedPreferences
import com.fundtracker.expenses.model.ExpenseData
import com.fundtracker.expenses.model.ExpenseItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("fund_tracker_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val syncService = GoogleDriveSyncService()

    companion object {
        private const val KEY_EXPENSE_ITEMS = "expense_items"
        private const val KEY_GDRIVE_URL = "gdrive_url"
        private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
    }

    suspend fun getExpenses(): List<ExpenseItem> = withContext(Dispatchers.IO) {
        val json = prefs.getString(KEY_EXPENSE_ITEMS, null) ?: return@withContext getDefaultInitialExpenses()
        try {
            val type = object : TypeToken<List<ExpenseItem>>() {}.type
            gson.fromJson<List<ExpenseItem>>(json, type) ?: getDefaultInitialExpenses()
        } catch (e: Exception) {
            getDefaultInitialExpenses()
        }
    }

    suspend fun saveExpenses(items: List<ExpenseItem>) = withContext(Dispatchers.IO) {
        val json = gson.toJson(items)
        prefs.edit().putString(KEY_EXPENSE_ITEMS, json).apply()
    }

    fun getGoogleDriveUrl(): String {
        return prefs.getString(KEY_GDRIVE_URL, "") ?: ""
    }

    fun saveGoogleDriveUrl(url: String) {
        prefs.edit().putString(KEY_GDRIVE_URL, url).apply()
    }

    fun getCurrencySymbol(): String {
        return prefs.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$"
    }

    fun saveCurrencySymbol(symbol: String) {
        prefs.edit().putString(KEY_CURRENCY_SYMBOL, symbol).apply()
    }

    fun getLastSyncTime(): Long {
        return prefs.getLong(KEY_LAST_SYNC_TIME, 0L)
    }

    fun saveLastSyncTime(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC_TIME, timestamp).apply()
    }

    suspend fun syncWithGoogleDrive(url: String): Result<ExpenseData> {
        val result = syncService.fetchExpensesFromDrive(url)
        if (result.isSuccess) {
            val data = result.getOrNull()
            if (data != null && data.items.isNotEmpty()) {
                saveExpenses(data.items)
                if (data.currency.isNotEmpty()) {
                    saveCurrencySymbol(data.currency)
                }
                saveLastSyncTime(System.currentTimeMillis())
            }
        }
        return result
    }

    fun exportAsJson(items: List<ExpenseItem>, currency: String): String {
        val data = ExpenseData(
            title = "Expenses Checklist",
            currency = currency,
            lastUpdated = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date()),
            items = items
        )
        return gson.toJson(data)
    }

    private fun getDefaultInitialExpenses(): List<ExpenseItem> {
        return listOf(
            ExpenseItem(name = "Groceries & Food Supplies", cost = 120.50, checked = true, category = "Groceries"),
            ExpenseItem(name = "Electric & Water Utilities", cost = 85.00, checked = false, category = "Utilities"),
            ExpenseItem(name = "Internet & WiFi Subscription", cost = 45.00, checked = true, category = "Bills"),
            ExpenseItem(name = "Fuel & Public Transit", cost = 60.00, checked = false, category = "Transportation"),
            ExpenseItem(name = "Apartment Rent", cost = 650.00, checked = false, category = "Housing")
        )
    }
}
