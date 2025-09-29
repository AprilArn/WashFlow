package com.aprilarn.washflow.ui.services

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.components.AddNewDataInputField
import com.aprilarn.washflow.ui.components.AddNewDataPanel
import com.aprilarn.washflow.ui.components.ColumnConfig
import com.aprilarn.washflow.ui.components.DataTablePanel
import com.aprilarn.washflow.ui.components.EditDataPanel
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun ServicesScreen(
    uiState: ServicesUiState,
    onAddServiceClick: (String, String) -> Unit,
    onEditServiceClick: (Services) -> Unit,
    onDeleteServiceClick: (Services) -> Unit,
    onServiceSelected: (Services) -> Unit,
    onDismissDialog: () -> Unit
) {
    // State untuk pencarian (search) data
    // var searchQuery by remember { mutableStateOf("") }
    // State untuk panel "Add New"
    var newServiceName by remember { mutableStateOf("") }
    var newServiceId by remember { mutableStateOf("") }

    val serviceCount = uiState.services.size
    val filteredServices = uiState.services.filter {
        it.serviceName.contains(newServiceName, ignoreCase = true)
                || it.serviceId.contains(newServiceName, ignoreCase = true)
    }

    // Dialog untuk Edit/Delete
    uiState.selectedService?.let { serviceToEdit ->
        var editedName by remember { mutableStateOf(serviceToEdit.serviceName) }

        LaunchedEffect(serviceToEdit) {
            editedName = serviceToEdit.serviceName
        }

        Dialog(onDismissRequest = onDismissDialog) {
            // ID service tidak bisa diedit karena merupakan primary key
            val editInputFields = listOf(
                AddNewDataInputField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = "Service Name"
                )
            )

            EditDataPanel(
                title = "Edit ${serviceToEdit.serviceName}",
                inputFields = editInputFields,
                onDoneClick = {
                    val updatedService = serviceToEdit.copy(serviceName = editedName)
                    onEditServiceClick(updatedService)
                },
                onDeleteClick = {
                    onDeleteServiceClick(serviceToEdit)
                }
            )
        }
    }


    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Panel Kiri: Tabel Data
        Box(modifier = Modifier.weight(2f)) {
            val serviceColumns = listOf(
                ColumnConfig<Services>(
                    header = "ID",
                    weight = 0.3f,
                    content = { service -> Text(service.serviceId, color = GrayBlue) }
                ),
                ColumnConfig<Services>(
                    header = "Service Name",
                    weight = 0.8f,
                    content = { service -> Text(service.serviceName, color = GrayBlue) }
                ),
                ColumnConfig<Services>(
                    header = "Edit",
                    weight = 0.1f,
                    content = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Service",
                            tint = GrayBlue.copy(alpha = 0.7f)
                        )
                    }
                )
            )

            DataTablePanel(
                title = "Service",
                itemCount = serviceCount,
                searchQuery = newServiceName,
                onSearchQueryChange = { newServiceName = it },
                searchPlaceholder = "Search by ID/Name",
                columns = serviceColumns,
                data = filteredServices,
                isLoading = uiState.isLoading,
                onRowClick = { service -> onServiceSelected(service) }
            )
        }

        // Panel Kanan: Tambah Data Baru
        Box(modifier = Modifier.weight(1f)) {
            val inputFields = listOf(
                AddNewDataInputField(
                    value = newServiceName,
                    onValueChange = { newServiceName = it },
                    label = "Nama Service"
                ),
                AddNewDataInputField(
                    value = newServiceId,
                    onValueChange = { newServiceId = it },
                    label = "ID Service"
                )
            )

            AddNewDataPanel(
                title = "Add New Service",
                subTitle = "Tambah layanan baru",
                inputFields = inputFields,
                onAddClick = {
                    onAddServiceClick(newServiceId, newServiceName)
                    newServiceId = ""
                    newServiceName = ""
                },
                addButtonText = "Add Service"
            )
        }
    }
}