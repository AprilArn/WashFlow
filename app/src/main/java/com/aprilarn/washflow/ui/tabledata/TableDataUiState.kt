package com.aprilarn.washflow.ui.tabledata

data class TableDataUiState(
    val customerCount: Int = 0,
    val serviceCount: Int = 0,
    val itemCount: Int = 0,
    val isLoading: Boolean = true
)