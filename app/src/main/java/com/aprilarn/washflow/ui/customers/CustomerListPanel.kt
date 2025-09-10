// com/aprilarn/washflow/ui/customers/CustomerListPanel.kt

package com.aprilarn.washflow.ui.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.ui.theme.GrayBlue


@Composable
fun CustomerListPanel(
    customers: List<Customers>,
    isLoading: Boolean,
    onEditClick: (Customers) -> Unit,
    onDeleteClick: (Customers) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.25f))
    ) {
        Column {
            // 1. Table Header (tidak berubah, akan selalu terlihat)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Name", modifier = Modifier.weight(0.45f), fontWeight = FontWeight.Bold, color = GrayBlue)
                Text("Contact", modifier = Modifier.weight(0.45f), fontWeight = FontWeight.Bold, color = GrayBlue)
                Text("Edit", modifier = Modifier.weight(0.1f), fontWeight = FontWeight.Bold, color = GrayBlue)
            }
            Divider(color = Color.White.copy(alpha = 0.5f))

            // 2. Table Rows (sekarang bisa menampilkan loading atau data)
            LazyColumn {
                if (isLoading) {
                    // Jika sedang loading, tampilkan satu baris berisi indicator
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                } else {
                    // Jika tidak loading, tampilkan daftar pelanggan
                    items(customers) { customer ->
                        CustomerListItem(
                            customer = customer,
                            onEditClick = { onEditClick(customer) },
                            onDeleteClick = { onDeleteClick(customer) }
                        )
                        Divider(
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ... (CustomerListItem dan Preview tidak perlu diubah)
@Composable
fun CustomerListItem(
    customer: Customers,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(customer.name, modifier = Modifier.weight(0.45f), color = GrayBlue)
        customer.contact?.let {
            Text(it, modifier = Modifier.weight(0.45f), color = GrayBlue)
        }
        Row(
            modifier = Modifier.weight(0.1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFF8BC34A), CircleShape)
                    .padding(0.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(16.dp))
            }
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFFF44336), CircleShape)
                    .padding(0.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 600)
@Composable
fun CustomerListPanelPreview() {
    val sampleCustomersForPreview = listOf(
        Customers("1", "Xxxxxxx Xxx Xxxxxxxx", "081xxxxxxx"),
        Customers("2", "Xxxxxxx Xxx Xxxxxxxx", "081xxxxxxx"),
        Customers("3", "Xxxxxxx Xxx Xxxxxxxx", "081xxxxxxx"),
        Customers("4", "Raphael", "081233334444")
    )

    Box(
        modifier = Modifier.background(Color(0xFF949494))
    ) {
        CustomerListPanel(
            customers = sampleCustomersForPreview,
            isLoading = false,
            onEditClick = {},
            onDeleteClick = {},
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 600)
@Composable
fun CustomerListPanelLoadingPreview() {
    Box(
        modifier = Modifier.background(Color(0xFF949494))
    ) {
        CustomerListPanel(
            customers = emptyList(), // Daftar kosong saat loading
            isLoading = true,        // Set isLoading ke true
            onEditClick = {},
            onDeleteClick = {},
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        )
    }
}