package com.fundtracker.expenses.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class ExpenseItem(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),

    @SerializedName("name")
    val name: String,

    @SerializedName("cost")
    val cost: Double,

    @SerializedName("checked")
    val checked: Boolean = false,

    @SerializedName("category")
    val category: String = "General",

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
