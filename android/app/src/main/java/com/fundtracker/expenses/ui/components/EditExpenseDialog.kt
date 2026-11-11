package com.fundtracker.expenses.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fundtracker.expenses.model.ExpenseItem
import com.fundtracker.expenses.ui.theme.GreenPrimary
import com.fundtracker.expenses.ui.theme.GreenPrimaryDark
import java.util.Locale

@Composable
fun EditExpenseDialog(
    item: ExpenseItem,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (ExpenseItem) -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var costText by remember { mutableStateOf(String.format(Locale.US, "%.2f", item.cost)) }
    var category by remember { mutableStateOf(item.category) }
    var nameError by remember { mutableStateOf(false) }
    var costError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Expense Item",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimaryDark
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("Item Name *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = GreenPrimary)
                    },
                    isError = nameError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        cursorColor = GreenPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = costText,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            costText = it
                            if (it.isNotBlank()) costError = false
                        }
                    },
                    label = { Text("Cost ($currency) *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Paid, contentDescription = null, tint = GreenPrimary)
                    },
                    isError = costError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        cursorColor = GreenPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Category, contentDescription = null, tint = GreenPrimary)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        cursorColor = GreenPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    val parsedCost = costText.toDoubleOrNull()
                    val isNameValid = trimmedName.isNotBlank()
                    val isCostValid = parsedCost != null && parsedCost >= 0

                    if (!isNameValid) nameError = true
                    if (!isCostValid) costError = true

                    if (isNameValid && isCostValid) {
                        onSave(
                            item.copy(
                                name = trimmedName,
                                cost = parsedCost!!,
                                category = category.trim().ifBlank { "General" }
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", color = Color(0xFF616161))
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}
