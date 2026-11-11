package com.fundtracker.expenses.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fundtracker.expenses.model.ExpenseItem
import com.fundtracker.expenses.ui.theme.GreenChecked
import com.fundtracker.expenses.ui.theme.GreenContainer
import com.fundtracker.expenses.ui.theme.GreenPrimary
import com.fundtracker.expenses.ui.theme.GreenPrimaryDark
import java.util.Locale

@Composable
fun ExpenseItemRow(
    item: ExpenseItem,
    currency: String,
    onToggleCheck: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alphaAnim by animateFloatAsState(
        targetValue = if (item.checked) 0.72f else 1f,
        label = "itemAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alphaAnim)
            .clickable { onToggleCheck() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.checked) Color(0xFFF1F8F2) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (item.checked) 1.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox with Green theme styling
            Checkbox(
                checked = item.checked,
                onCheckedChange = { onToggleCheck() },
                colors = CheckboxDefaults.colors(
                    checkedColor = GreenChecked,
                    uncheckedColor = GreenPrimary.copy(alpha = 0.6f),
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Item Name & Category
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (item.checked) FontWeight.Normal else FontWeight.SemiBold,
                        textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (item.checked) Color(0xFF556B58) else Color(0xFF1E271F)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.category.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (item.checked) Color(0xFFE0ECE1) else GreenContainer
                    ) {
                        Text(
                            text = item.category,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = GreenPrimaryDark
                            )
                        )
                    }
                }
            }

            // Cost Display
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = String.format(Locale.US, "%s%.2f", currency, item.cost),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (item.checked) GreenChecked else Color(0xFF2E7D32),
                        textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None
                    )
                )

                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Item",
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Item",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}
