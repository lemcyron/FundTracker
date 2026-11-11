package com.fundtracker.expenses.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fundtracker.expenses.model.ExpenseItem
import com.fundtracker.expenses.ui.components.AddExpenseDialog
import com.fundtracker.expenses.ui.components.BottomSummaryBar
import com.fundtracker.expenses.ui.components.EditExpenseDialog
import com.fundtracker.expenses.ui.components.ExpenseItemRow
import com.fundtracker.expenses.ui.components.SettingsDialog
import com.fundtracker.expenses.ui.theme.GreenBackgroundLight
import com.fundtracker.expenses.ui.theme.GreenContainer
import com.fundtracker.expenses.ui.theme.GreenPrimary
import com.fundtracker.expenses.ui.theme.GreenPrimaryDark
import com.fundtracker.expenses.viewmodel.ExpenseFilter
import com.fundtracker.expenses.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerScreen(
    viewModel: ExpenseViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val totals by viewModel.totals.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ExpenseItem?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    // Filter items according to search query and filter selection
    val filteredItems = uiState.items.filter { item ->
        val matchesSearch = uiState.searchQuery.isBlank() ||
                item.name.contains(uiState.searchQuery, ignoreCase = true) ||
                item.category.contains(uiState.searchQuery, ignoreCase = true)

        val matchesFilter = when (uiState.selectedFilter) {
            ExpenseFilter.ALL -> true
            ExpenseFilter.CHECKED -> item.checked
            ExpenseFilter.UNCHECKED -> !item.checked
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "FundTracker",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = "Expenses Checklist Tracker",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFC8E6C9),
                                fontSize = 12.sp
                            )
                        )
                    }
                },
                actions = {
                    // Quick Sync button for Google Drive
                    IconButton(
                        onClick = {
                            if (uiState.googleDriveUrl.isNotBlank()) {
                                viewModel.syncWithGoogleDrive()
                            } else {
                                showSettingsDialog = true
                            }
                        }
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync from Drive",
                                tint = Color.White
                            )
                        }
                    }

                    // Settings & Actions button (Check All / Uncheck All / GDrive Sync)
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimaryDark
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Item") },
                text = { Text("Add Item", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            // Requirement: Bottom part showing total of all amounts, total for checked and unchecked
            BottomSummaryBar(
                totals = totals,
                currency = uiState.currency
            )
        },
        containerColor = GreenBackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter and Search Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter Chips (All, Checked, Unchecked)
                FilterChip(
                    selected = (uiState.selectedFilter == ExpenseFilter.ALL),
                    onClick = { viewModel.setFilter(ExpenseFilter.ALL) },
                    label = { Text("All (${totals.totalCount})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = (uiState.selectedFilter == ExpenseFilter.CHECKED),
                    onClick = { viewModel.setFilter(ExpenseFilter.CHECKED) },
                    label = { Text("Checked (${totals.checkedCount})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = (uiState.selectedFilter == ExpenseFilter.UNCHECKED),
                    onClick = { viewModel.setFilter(ExpenseFilter.UNCHECKED) },
                    label = { Text("Unchecked (${totals.uncheckedCount})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }

            // Checklist Content
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(GreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState.items.isEmpty()) "No expense items yet" else "No matching items",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E5B32)
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (uiState.items.isEmpty())
                                "Tap '+ Add Item' below or sync from Google Drive in Settings."
                            else
                                "Try changing your filter or search query.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF757575),
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        ExpenseItemRow(
                            item = item,
                            currency = uiState.currency,
                            onToggleCheck = { viewModel.toggleItemCheck(item.id) },
                            onEdit = { editingItem = item },
                            onDelete = { viewModel.deleteItem(item) }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        AddExpenseDialog(
            currency = uiState.currency,
            onDismiss = { showAddDialog = false },
            onAdd = { name, cost, category ->
                viewModel.addItem(name, cost, category)
                showAddDialog = false
            }
        )
    }

    editingItem?.let { item ->
        EditExpenseDialog(
            item = item,
            currency = uiState.currency,
            onDismiss = { editingItem = null },
            onSave = { updated ->
                viewModel.updateItem(updated)
                editingItem = null
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentGoogleDriveUrl = uiState.googleDriveUrl,
            currentCurrency = uiState.currency,
            isLoading = uiState.isLoading,
            onDismiss = { showSettingsDialog = false },
            onCheckAll = { viewModel.checkAllItems() },
            onUncheckAll = { viewModel.uncheckAllItems() },
            onClearAll = { viewModel.clearAllItems() },
            onSaveCurrency = { viewModel.setCurrency(it) },
            onSaveGoogleDriveUrl = { viewModel.setGoogleDriveUrl(it) },
            onSyncGoogleDrive = { viewModel.syncWithGoogleDrive(it) },
            onGetExportJson = { viewModel.getExportJson() }
        )
    }
}
