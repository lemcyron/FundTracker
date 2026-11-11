package com.fundtracker.expenses.model

import com.google.gson.annotations.SerializedName

data class ExpenseData(
    @SerializedName("title")
    val title: String = "Expense Tracker",

    @SerializedName("currency")
    val currency: String = "$",

    @SerializedName("lastUpdated")
    val lastUpdated: String = "",

    @SerializedName("items")
    val items: List<ExpenseItem> = emptyList()
)
