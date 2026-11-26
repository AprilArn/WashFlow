package com.aprilarn.washflow.ui.feature.masterdata.services

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.R
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.core.components.AddNewDataInputField
import com.aprilarn.washflow.ui.core.components.AddNewDataPanel
import com.aprilarn.washflow.ui.core.components.ColumnConfig
import com.aprilarn.washflow.ui.core.components.DataTablePanel
import com.aprilarn.washflow.ui.core.components.DeleteConfirmationDialog
import com.aprilarn.washflow.ui.core.components.EditDataPanel
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.PastelGreen
import com.aprilarn.washflow.ui.core.theme.PastelGreenBg
import com.aprilarn.washflow.ui.core.theme.PastelGreenDark

@Composable
fun ServicesScreen(
    uiState: ServicesUiState,
    onAddServiceClick: (String, String) -> Unit,
    onEditServiceClick: (Services) -> Unit,
    onDeleteServiceClick: (Services) -> Unit,
    onServiceSelected: (Services) -> Unit,
    onDismissDialog: () -> Unit
) {
    var newServiceName by rememberSaveable { mutableStateOf("") }
    var newServiceId by rememberSaveable { mutableStateOf("") }

    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }

    val serviceCount = uiState.services.size
    val filteredServices = uiState.services.filter {
        it.serviceName.contains(newServiceName, ignoreCase = true)
                || it.serviceId.contains(newServiceName, ignoreCase = true)
    }

    // Dialog untuk Edit/Delete
    uiState.selectedService?.let { serviceToEdit ->
        var editedName by rememberSaveable(serviceToEdit.serviceId) { mutableStateOf(serviceToEdit.serviceName) }

        if (showDeleteConfirmation) {
            DeleteConfirmationDialog(
                itemName = serviceToEdit.serviceName,
                onConfirm = {
                    onDeleteServiceClick(serviceToEdit)
                    showDeleteConfirmation = false
                    onDismissDialog()
                },
                onDismiss = {
                    showDeleteConfirmation = false
                }
            )
        } else {
            Dialog(onDismissRequest = onDismissDialog) {
                val editInputFields = listOf(
                    AddNewDataInputField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = stringResource(R.string.master_services_name_label)
                    )
                )

                EditDataPanel(
                    title = stringResource(R.string.master_edit_item_title, serviceToEdit.serviceName),
                    inputFields = editInputFields,
                    onDoneClick = {
                        val updatedService = serviceToEdit.copy(serviceName = editedName)
                        onEditServiceClick(updatedService)
                    },
                    onDeleteClick = {
                        showDeleteConfirmation = true
                    },
                    accentHint = "Service"
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Panel Kiri: Tabel Data
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
        ) {
            val serviceColumns = listOf(
                ColumnConfig<Services>(
                    header = stringResource(R.string.master_services_col_id),
                    weight = 0.28f,
                    content = { service ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PastelGreenBg
                        ) {
                            Text(
                                text = service.serviceId,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PastelGreenDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                ),
                ColumnConfig<Services>(
                    header = stringResource(R.string.master_services_col_name),
                    weight = 0.80f,
                    content = { service ->
                        Text(
                            text = service.serviceName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                    }
                ),
                ColumnConfig<Services>(
                    header = stringResource(R.string.common_edit),
                    weight = 0.12f,
                    content = {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(PastelGreenBg, RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.master_services_edit_cd),
                                tint = PastelGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )
            )

            DataTablePanel(
                title = stringResource(R.string.master_services_title),
                itemCount = serviceCount,
                searchQuery = newServiceName,
                onSearchQueryChange = { newServiceName = it },
                searchPlaceholder = stringResource(R.string.master_services_search_placeholder),
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
                    label = stringResource(R.string.master_services_name_label)
                ),
                AddNewDataInputField(
                    value = newServiceId,
                    onValueChange = { newServiceId = it },
                    label = stringResource(R.string.master_services_id_label)
                )
            )

            AddNewDataPanel(
                title = stringResource(R.string.master_services_add_title),
                subTitle = stringResource(R.string.master_services_add_subtitle),
                inputFields = inputFields,
                onAddClick = {
                    onAddServiceClick(newServiceId, newServiceName)
                    newServiceId = ""
                    newServiceName = ""
                },
                addButtonText = stringResource(R.string.master_services_add_button)
            )
        }
    }
}
