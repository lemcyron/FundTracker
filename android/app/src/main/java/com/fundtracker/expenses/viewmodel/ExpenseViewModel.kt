package com.fundtracker.expenses.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fundtracker.expenses.data.ExpenseRepository
import com.fundtracker.expenses.model.ExpenseItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExpenseTotals(
    val totalAll: Double = 0.0,
    val totalChecked: Double = 0.0,
    val totalUnchecked: Double = 0.0,
    val totalCount: Int = 0,
    val checkedCount: Int = 0,
    val uncheckedCount: Int = 0,
    val percentChecked: Float = 0f
)

data class UiState(
    val items: List<ExpenseItem> = emptyList(),
    val currency: String = "$",
    val googleDriveUrl: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    val selectedFilter: ExpenseFilter = ExpenseFilter.ALL
)

enum class ExpenseFilter {
    ALL, CHECKED, UNCHECKED
}

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExpenseRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val totals: StateFlow<ExpenseTotals> = _uiState.combine(MutableStateFlow(Unit)) { state, _ ->
        val items = state.items
        var allSum = 0.0
        var checkedSum = 0.0
        var uncheckedSum = 0.0
        var checkedCount = 0
        var uncheckedCount = 0

        for (item in items) {
            allSum += item.cost
            if (item.checked) {
                checkedSum += item.cost
                checkedCount++
            } else {
                uncheckedSum += item.cost
                uncheckedCount++
            }
        }

        val totalCount = items.size
        val percent = if (totalCount > 0) (checkedCount.toFloat() / totalCount.toFloat()) else 0f

        ExpenseTotals(
            totalAll = allSum,
            totalChecked = checkedSum,
            totalUnchecked = uncheckedSum,
            totalCount = totalCount,
            checkedCount = checkedCount,
            uncheckedCount = uncheckedCount,
            percentChecked = percent
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExpenseTotals())

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val items = repository.getExpenses()
            val currency = repository.getCurrencySymbol()
            val driveUrl = repository.getGoogleDriveUrl()
            _uiState.update {
                it.copy(
                    items = items,
                    currency = currency,
                    googleDriveUrl = driveUrl,
                    isLoading = false
                )
            }
        }
    }

    fun addItem(name: String, cost: Double, category: String = "General") {
        if (name.isBlank() || cost < 0) return
        viewModelScope.launch {
            val newItem = ExpenseItem(
                name = name.trim(),
                cost = cost,
                checked = false,
                category = category.trim().ifBlank { "General" }
            )
            val updated = listOf(newItem) + _uiState.value.items
            _uiState.update { it.copy(items = updated, successMessage = "Added '${newItem.name}'") }
            repository.saveExpenses(updated)
        }
    }

    fun updateItem(updatedItem: ExpenseItem) {
        viewModelScope.launch {
            val updated = _uiState.value.items.map {
                if (it.id == updatedItem.id) updatedItem else it
            }
            _uiState.update { it.copy(items = updated) }
            repository.saveExpenses(updated)
        }
    }

    fun toggleItemCheck(id: String) {
        viewModelScope.launch {
            val updated = _uiState.value.items.map {
                if (it.id == id) it.copy(checked = !it.checked) else it
            }
            _uiState.update { it.copy(items = updated) }
            repository.saveExpenses(updated)
        }
    }

    fun deleteItem(item: ExpenseItem) {
        viewModelScope.launch {
            val updated = _uiState.value.items.filter { it.id != item.id }
            _uiState.update { it.copy(items = updated, successMessage = "Removed '${item.name}'") }
            repository.saveExpenses(updated)
        }
    }

    /**
     * Requirement: Functions to check all items in a settings menu
     */
    fun checkAllItems() {
        viewModelScope.launch {
            val updated = _uiState.value.items.map { it.copy(checked = true) }
            _uiState.update { it.copy(items = updated, successMessage = "All items checked!") }
            repository.saveExpenses(updated)
        }
    }

    /**
     * Requirement: Functions to uncheck all items in a settings menu
     */
    fun uncheckAllItems() {
        viewModelScope.launch {
            val updated = _uiState.value.items.map { it.copy(checked = false) }
            _uiState.update { it.copy(items = updated, successMessage = "All items unchecked!") }
            repository.saveExpenses(updated)
        }
    }

    fun clearAllItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(items = emptyList(), successMessage = "Checklist cleared") }
            repository.saveExpenses(emptyList())
        }
    }

    fun setCurrency(newCurrency: String) {
        val symbol = newCurrency.trim().ifBlank { "$" }
        viewModelScope.launch {
            repository.saveCurrencySymbol(symbol)
            _uiState.update { it.copy(currency = symbol, successMessage = "Currency set to $symbol") }
        }
    }

    fun setGoogleDriveUrl(url: String) {
        val trimmed = url.trim()
        repository.saveGoogleDriveUrl(trimmed)
        _uiState.update { it.copy(googleDriveUrl = trimmed) }
    }

    /**
     * Requirement: Refer to a JSON file in Google Drive
     */
    fun syncWithGoogleDrive(urlToSync: String? = null) {
        val targetUrl = urlToSync?.trim() ?: _uiState.value.googleDriveUrl
        if (targetUrl.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a Google Drive link or file ID") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = repository.syncWithGoogleDrive(targetUrl)
            if (result.isSuccess) {
                val data = result.getOrNull()
                val items = data?.items ?: emptyList()
                val currency = data?.currency?.ifBlank { _uiState.value.currency } ?: _uiState.value.currency
                repository.saveGoogleDriveUrl(targetUrl)
                _uiState.update {
                    it.copy(
                        items = items,
                        currency = currency,
                        googleDriveUrl = targetUrl,
                        isLoading = false,
                        successMessage = "Successfully synced ${items.size} items from Google Drive!"
                    )
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown sync error"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Sync Failed: $errorMsg"
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setFilter(filter: ExpenseFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun getExportJson(): String {
        return repository.exportAsJson(_uiState.value.items, _uiState.value.currency)
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
