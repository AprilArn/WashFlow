package com.aprilarn.washflow.ui.aiagent.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.text.withStyle
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.aiagent.AiAgentAction
import com.aprilarn.washflow.ui.theme.*
import com.aprilarn.washflow.utils.CurrencyUtils
import com.aprilarn.washflow.utils.StringSimilarityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ActionConfirmationCard(
    action: AiAgentAction,
    customers: List<Customers> = emptyList(),
    items: List<Items> = emptyList(),
    services: List<Services> = emptyList(),
    onConfirm: (AiAgentAction?) -> Unit,
    onCancel: () -> Unit
) {
    var editedName by remember(action) {
        mutableStateOf(
            when (action) {
                is AiAgentAction.AddCustomer -> action.name
                is AiAgentAction.DeleteCustomer -> action.name
                is AiAgentAction.AddItem -> action.itemName
                is AiAgentAction.DeleteItem -> action.itemName
                else -> ""
            }
        )
    }
    var editedValue by remember(action) {
        mutableStateOf(
            when (action) {
                is AiAgentAction.AddCustomer -> action.phoneNumber
                is AiAgentAction.DeleteCustomer -> action.contact
                is AiAgentAction.AddItem -> action.itemPrice.toString().replace(".0", "")
                else -> ""
            }
        )
    }

    var editedServiceName by remember(action) {
        mutableStateOf(
            when (action) {
                is AiAgentAction.AddItem -> action.serviceName
                is AiAgentAction.DeleteItem -> action.serviceName
                else -> ""
            }
        )
    }

    val selectedServiceId by remember(editedServiceName, services) {
        derivedStateOf {
            services.find {
                it.serviceName.equals(editedServiceName, ignoreCase = true)
            }?.serviceId ?: ""
        }
    }

    val selectedCustomerId by remember(editedName, editedValue, customers) {
        derivedStateOf {
            if (action is AiAgentAction.DeleteCustomer) {
                customers.find {
                    it.name.equals(editedName, ignoreCase = true) &&
                    (it.contact ?: "") == editedValue
                }?.customerId ?: ""
            } else ""
        }
    }

    val selectedItem by remember(editedName, editedServiceName, items, services) {
        derivedStateOf {
            if (action is AiAgentAction.DeleteItem) {
                items.find { item ->
                    item.itemName.equals(editedName, ignoreCase = true) &&
                    (editedServiceName.isEmpty() || services.find { it.serviceId == item.serviceId }?.serviceName?.equals(editedServiceName, ignoreCase = true) == true)
                } ?: items.find {
                    it.itemName.equals(editedName, ignoreCase = true)
                }
            } else null
        }
    }

    // Auto-match for DeleteCustomer
    LaunchedEffect(action, customers) {
        if (action is AiAgentAction.DeleteCustomer && selectedCustomerId.isEmpty()) {
            val match = withContext(Dispatchers.Default) {
                customers.find {
                    action.contact.isNotBlank() && it.contact == action.contact
                } ?: customers.find {
                    action.name.isNotBlank() && it.name.equals(action.name, ignoreCase = true)
                } ?: customers.find {
                    action.contact.isNotBlank() && it.contact?.contains(action.contact) == true
                } ?: customers.find {
                    action.name.isNotBlank() && it.name.contains(action.name, ignoreCase = true)
                } ?: customers.find {
                    action.name.isNotBlank() && action.name.contains(it.name, ignoreCase = true)
                } ?: customers.asSequence()
                    .map { customer ->
                        val nameScore = if (action.name.isNotBlank()) StringSimilarityUtils.similarityScore(action.name, customer.name) else 0.0
                        val contactScore = if (action.contact.isNotBlank() && !customer.contact.isNullOrBlank()) StringSimilarityUtils.similarityScore(action.contact, customer.contact!!) else 0.0
                        customer to maxOf(nameScore, contactScore)
                    }
                    .filter { it.second > 0.6 }
                    .maxByOrNull { it.second }
                    ?.first
            }

            if (match != null) {
                editedName = match.name
                editedValue = match.contact ?: ""
            }
        }
    }

    // Auto-match for DeleteItem
    LaunchedEffect(action, items, services) {
        if (action is AiAgentAction.DeleteItem && selectedItem == null) {
            val match = withContext(Dispatchers.Default) {
                val serviceToMatch = action.serviceName

                items.find { item ->
                    item.itemName.equals(action.itemName, ignoreCase = true) &&
                    (serviceToMatch.isEmpty() || services.find { it.serviceId == item.serviceId }?.serviceName?.equals(serviceToMatch, ignoreCase = true) == true)
                } ?: items.find {
                    it.itemName.equals(action.itemName, ignoreCase = true)
                } ?: items.find {
                    it.itemName.contains(action.itemName, ignoreCase = true)
                } ?: items.find {
                    action.itemName.contains(it.itemName, ignoreCase = true)
                } ?: items.asSequence()
                    .map { item ->
                        val score = StringSimilarityUtils.similarityScore(action.itemName, item.itemName)
                        item to score
                    }
                    .filter { it.second > 0.6 }
                    .maxByOrNull { it.second }
                    ?.first
            }

            if (match != null) {
                editedName = match.itemName
                if (action.serviceName.isEmpty()) {
                    editedServiceName = services.find { it.serviceId == match.serviceId }?.serviceName ?: ""
                }
            }
        }
    }

    // Auto-match for AddItem (Service)
    LaunchedEffect(action, services) {
        if (action is AiAgentAction.AddItem && selectedServiceId.isEmpty()) {
            val match = withContext(Dispatchers.Default) {
                services.find {
                    it.serviceName.equals(action.serviceName, ignoreCase = true)
                } ?: services.find {
                    it.serviceName.contains(action.serviceName, ignoreCase = true)
                } ?: services.find {
                    action.serviceName.contains(it.serviceName, ignoreCase = true)
                } ?: services.asSequence()
                    .map { service ->
                        val score = StringSimilarityUtils.similarityScore(action.serviceName, service.serviceName)
                        service to score
                    }
                    .filter { it.second > 0.6 }
                    .maxByOrNull { it.second }
                    ?.first
            }

            if (match != null) {
                editedServiceName = match.serviceName
            }
        }
    }

    var isNameDropdownExpanded by remember { mutableStateOf(false) }
    var isPhoneDropdownExpanded by remember { mutableStateOf(false) }
    var isServiceDropdownExpanded by remember { mutableStateOf(false) }
    var nameTextFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var phoneTextFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var serviceTextFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    val description = when (action) {
        is AiAgentAction.Navigate -> buildAnnotatedString {
            append("Go to ")
            withStyle(style = SpanStyle(color = SkyBlue, fontWeight = FontWeight.Bold)) {
                append(action.destination.label)
            }
            append(" page?")
        }
        is AiAgentAction.AddCustomer -> buildAnnotatedString {
            append("Add ")
            withStyle(style = SpanStyle(color = GrayBlue, fontWeight = FontWeight.Bold)) {
                append(editedName)
            }
            append(" as a new customer?")
        }
        is AiAgentAction.AddItem -> buildAnnotatedString {
            append("Add laundry item ")
            withStyle(style = SpanStyle(color = GrayBlue, fontWeight = FontWeight.Bold)) {
                append(editedName)
            }
            append(" to ")
            withStyle(style = SpanStyle(color = GrayBlue, fontWeight = FontWeight.Bold)) {
                append(editedServiceName)
            }
            append(" with price ")
            withStyle(style = SpanStyle(color = SkyBlue, fontWeight = FontWeight.Bold)) {
                append(CurrencyUtils.formatRupiahWithSymbol(editedValue.toDoubleOrNull() ?: 0.0))
            }
            append("?")
        }
        is AiAgentAction.DeleteCustomer -> buildAnnotatedString {
            append("Delete customer ")
            withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                append(editedName)
            }
            append("?")
        }
        is AiAgentAction.DeleteItem -> buildAnnotatedString {
            append("Delete laundry item ")
            withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                append(editedName)
            }
            append("?")
        }
        is AiAgentAction.Unknown -> AnnotatedString(action.message)
        else -> AnnotatedString("")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Aira needs your confirmation:",
                style = MaterialTheme.typography.labelSmall,
                color = Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (action is AiAgentAction.AddCustomer || action is AiAgentAction.DeleteCustomer || action is AiAgentAction.DeleteItem || action is AiAgentAction.AddItem) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box {
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = {
                                editedName = it
                                isNameDropdownExpanded = true
                                isPhoneDropdownExpanded = false
                                isServiceDropdownExpanded = false
                            },
                            label = { Text(if (action is AiAgentAction.DeleteItem || action is AiAgentAction.AddItem) "Item Name" else "Customer Name", fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    nameTextFieldSize = coordinates.size.toSize()
                                },
                            textStyle = MaterialTheme.typography.bodyMedium,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrayBlue,
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )

                        if (isNameDropdownExpanded && editedName.isNotEmpty()) {
                            if (action is AiAgentAction.DeleteItem) {
                                val filteredItems = items.filter {
                                    it.itemName.contains(editedName, ignoreCase = true)
                                }

                                if (filteredItems.isNotEmpty()) {
                                    Popup(
                                        onDismissRequest = { isNameDropdownExpanded = false },
                                        offset = IntOffset(x = 0, y = nameTextFieldSize.height.toInt()),
                                        properties = PopupProperties(focusable = false)
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .width(with(LocalDensity.current) { nameTextFieldSize.width.toDp() })
                                                .padding(top = 4.dp)
                                                .heightIn(max = 150.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            shadowElevation = 4.dp,
                                            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                            color = Color.White
                                        ) {
                                            LazyColumn {
                                                items(filteredItems) { item ->
                                                    DropdownMenuItem(
                                                        text = {
                                                            Column {
                                                                Text(item.itemName, style = MaterialTheme.typography.bodyMedium)
                                                                Text(CurrencyUtils.formatRupiahWithSymbol(item.itemPrice), style = MaterialTheme.typography.labelSmall, color = Gray)
                                                            }
                                                        },
                                                        onClick = {
                                                            editedName = item.itemName
                                                            isNameDropdownExpanded = false
                                                        }
                                                    )
                                                    HorizontalDivider(color = Color(0xFFEEEEEE))
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                val filteredCustomers = customers.filter {
                                    it.name.contains(editedName, ignoreCase = true)
                                }

                                if (filteredCustomers.isNotEmpty()) {
                                    Popup(
                                        onDismissRequest = { isNameDropdownExpanded = false },
                                        offset = IntOffset(x = 0, y = nameTextFieldSize.height.toInt()),
                                        properties = PopupProperties(focusable = false)
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .width(with(LocalDensity.current) { nameTextFieldSize.width.toDp() })
                                                .padding(top = 4.dp)
                                                .heightIn(max = 150.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            shadowElevation = 4.dp,
                                            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                            color = Color.White
                                        ) {
                                            LazyColumn {
                                                items(filteredCustomers) { customer ->
                                                    DropdownMenuItem(
                                                        text = {
                                                            Column {
                                                                Text(customer.name, style = MaterialTheme.typography.bodyMedium)
                                                                if (!customer.contact.isNullOrEmpty()) {
                                                                    Text(customer.contact, style = MaterialTheme.typography.labelSmall, color = Gray)
                                                                }
                                                            }
                                                        },
                                                        onClick = {
                                                            editedName = customer.name
                                                            editedValue = customer.contact ?: ""
                                                            isNameDropdownExpanded = false
                                                        }
                                                    )
                                                    HorizontalDivider(color = Color(0xFFEEEEEE))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (action !is AiAgentAction.DeleteItem) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Box {
                            OutlinedTextField(
                                value = editedValue,
                                onValueChange = {
                                    editedValue = it
                                    if (action is AiAgentAction.DeleteCustomer) {
                                        isPhoneDropdownExpanded = true
                                        isNameDropdownExpanded = false
                                        isServiceDropdownExpanded = false
                                    }
                                },
                                label = {
                                    Text(
                                        when (action) {
                                            is AiAgentAction.AddCustomer, is AiAgentAction.DeleteCustomer -> "Phone Number"
                                            is AiAgentAction.AddItem -> "Price"
                                            else -> "Customer ID"
                                        },
                                        fontSize = 12.sp
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onGloballyPositioned { coordinates ->
                                        phoneTextFieldSize = coordinates.size.toSize()
                                    },
                                textStyle = MaterialTheme.typography.bodyMedium,
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = if (action is AiAgentAction.AddItem) androidx.compose.ui.text.input.KeyboardType.Number else androidx.compose.ui.text.input.KeyboardType.Phone
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GrayBlue,
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                )
                            )

                            if (isPhoneDropdownExpanded && editedValue.isNotEmpty() && action is AiAgentAction.DeleteCustomer) {
                                val filteredCustomers = customers.filter {
                                    it.contact?.contains(editedValue, ignoreCase = true) == true
                                }

                                if (filteredCustomers.isNotEmpty()) {
                                    Popup(
                                        onDismissRequest = { isPhoneDropdownExpanded = false },
                                        offset = IntOffset(x = 0, y = phoneTextFieldSize.height.toInt()),
                                        properties = PopupProperties(focusable = false)
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .width(with(LocalDensity.current) { phoneTextFieldSize.width.toDp() })
                                                .padding(top = 4.dp)
                                                .heightIn(max = 150.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            shadowElevation = 4.dp,
                                            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                            color = Color.White
                                        ) {
                                            LazyColumn {
                                                items(filteredCustomers) { customer ->
                                                    DropdownMenuItem(
                                                        text = {
                                                            Column {
                                                                Text(customer.name, style = MaterialTheme.typography.bodyMedium)
                                                                if (!customer.contact.isNullOrEmpty()) {
                                                                    Text(customer.contact, style = MaterialTheme.typography.labelSmall, color = Gray)
                                                                }
                                                            }
                                                        },
                                                        onClick = {
                                                            editedName = customer.name
                                                            editedValue = customer.contact ?: ""
                                                            isPhoneDropdownExpanded = false
                                                        }
                                                    )
                                                    HorizontalDivider(color = Color(0xFFEEEEEE))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (action is AiAgentAction.AddItem) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box {
                                OutlinedTextField(
                                    value = editedServiceName,
                                    onValueChange = {
                                        editedServiceName = it
                                        isServiceDropdownExpanded = true
                                        isNameDropdownExpanded = false
                                        isPhoneDropdownExpanded = false
                                    },
                                    label = { Text("Service", fontSize = 12.sp) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onGloballyPositioned { coordinates ->
                                            serviceTextFieldSize = coordinates.size.toSize()
                                        },
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GrayBlue,
                                        unfocusedBorderColor = Color(0xFFE0E0E0)
                                    )
                                )

                                if (isServiceDropdownExpanded && editedServiceName.isNotEmpty()) {
                                    val filteredServices = services.filter {
                                        it.serviceName.contains(editedServiceName, ignoreCase = true)
                                    }

                                    if (filteredServices.isNotEmpty()) {
                                        Popup(
                                            onDismissRequest = { isServiceDropdownExpanded = false },
                                            offset = IntOffset(x = 0, y = serviceTextFieldSize.height.toInt()),
                                            properties = PopupProperties(focusable = false)
                                        ) {
                                            Surface(
                                                modifier = Modifier
                                                    .width(with(LocalDensity.current) { serviceTextFieldSize.width.toDp() })
                                                    .padding(top = 4.dp)
                                                    .heightIn(max = 150.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                shadowElevation = 4.dp,
                                                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                                color = Color.White
                                            ) {
                                                LazyColumn {
                                                    items(filteredServices) { service ->
                                                        DropdownMenuItem(
                                                            text = { Text(service.serviceName, style = MaterialTheme.typography.bodyMedium) },
                                                            onClick = {
                                                                editedServiceName = service.serviceName
                                                                isServiceDropdownExpanded = false
                                                            }
                                                        )
                                                        HorizontalDivider(color = Color(0xFFEEEEEE))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (selectedItem != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFF0F0F0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Item Details",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        val serviceName = services.find { it.serviceId == selectedItem!!.serviceId }?.serviceName ?: "Unknown Service"
                                        Text(
                                            text = selectedItem!!.itemName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = serviceName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Gray
                                        )
                                    }
                                    Text(
                                        text = CurrencyUtils.formatRupiahWithSymbol(selectedItem!!.itemPrice),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GrayBlue
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MainFontBlack
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = Gray, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val resultAction = when (action) {
                            is AiAgentAction.AddCustomer -> AiAgentAction.AddCustomer(editedName, editedValue)
                            is AiAgentAction.DeleteCustomer -> AiAgentAction.DeleteCustomer(editedName, editedValue, selectedCustomerId)
                            is AiAgentAction.AddItem -> AiAgentAction.AddItem(editedName, editedValue.toDoubleOrNull() ?: 0.0, editedServiceName, selectedServiceId)
                            is AiAgentAction.DeleteItem -> AiAgentAction.DeleteItem(editedName, editedServiceName, selectedItem?.itemId ?: "")
                            else -> null
                        }
                        onConfirm(resultAction)
                    },
                    enabled = when (action) {
                        is AiAgentAction.DeleteCustomer -> selectedCustomerId.isNotEmpty()
                        is AiAgentAction.AddItem -> editedName.isNotBlank() && selectedServiceId.isNotEmpty()
                        is AiAgentAction.DeleteItem -> selectedItem != null
                        else -> true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (action is AiAgentAction.DeleteCustomer || action is AiAgentAction.DeleteItem) Color(0xFFEF5350) else GrayBlue
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        if (action is AiAgentAction.DeleteCustomer || action is AiAgentAction.DeleteItem) "Delete" else "Confirm",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}