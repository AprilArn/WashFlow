package com.aprilarn.washflow.ui.aiagent

import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.theme.*
import com.aprilarn.washflow.utils.CurrencyUtils
import com.aprilarn.washflow.utils.MarkdownUtils
import com.aprilarn.washflow.utils.StringSimilarityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun AiMessageHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = GrayBlue,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Aira",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = GrayBlue
        )
    }
}

@Composable
fun VoiceAgentOverlay(
    status: VoiceAgentStatus,
    modifier: Modifier = Modifier
) {
    if (status == VoiceAgentStatus.IDLE) return

    val text = when (status) {
        VoiceAgentStatus.LISTENING -> "Listening..."
        VoiceAgentStatus.PROCESSING -> "Processing..."
        else -> ""
    }

    val icon = when (status) {
        VoiceAgentStatus.LISTENING -> Icons.Default.Mic
        VoiceAgentStatus.PROCESSING -> Icons.Default.Sync
        else -> Icons.Default.AutoAwesome
    }

    Surface(
        modifier = modifier
            .width(280.dp)
            .height(64.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Icon Area
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(64.dp)
                    .background(GrayBlue.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GrayBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MainFontBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

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
                // Prioritas pencarian:
                // 1. Exact match di Phone Number (Contact)
                // 2. Exact match di Name (Case Insensitive)
                // 3. Partial match di Name
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

@Composable
fun TypewriterText(
    text: String,
    progress: Int, // Drives the typewriter from ViewModel
    modifier: Modifier = Modifier,
    isNewMessage: Boolean = true,
    onTextUpdate: () -> Unit = {}
) {
    val displayedText = remember(text, progress) {
        if (progress < 0 || progress >= text.length) text else text.take(progress)
    }
    
    val view = LocalView.current

    // Handle haptics and scroll-updates based on progress
    LaunchedEffect(progress) {
        if (progress > 0 && progress < text.length) {
            val currentChar = text[progress - 1]
            if (currentChar.isWhitespace()) {
                val prevChar = if (progress > 1) text[progress - 2] else null
                if (prevChar == null || !prevChar.isWhitespace()) {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
            }
            onTextUpdate()
        }
        
        if (progress >= text.length && isNewMessage) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }


    Text(
        text = MarkdownUtils.parseMarkdown(displayedText),
        style = MaterialTheme.typography.bodyMedium,
        color = MainFontBlack,
        lineHeight = 20.sp,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun PromptItem(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .clickable { }
            .padding(12.dp)
    ) {
        Text(text, color = MainFontBlack, fontSize = 13.sp)
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    profilePictureUrl: String?,
    isAlreadyAnimated: Boolean = false,
    progress: Int = -1, // New: Drives typewriter from ViewModel
    customers: List<Customers> = emptyList(),
    items: List<Items> = emptyList(),
    services: List<Services> = emptyList(),
    onConfirmAction: (AiAgentAction?) -> Unit = {},
    onCancelAction: () -> Unit = {},
    onTextUpdate: () -> Unit = {}
) {
    if (message.isUser) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                    .background(Color(0xFFF0F2F5))
                    .padding(12.dp)
            ) {
                Text(
                    text = MarkdownUtils.parseMarkdown(message.text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MainFontBlack
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            AsyncImage(
                model = profilePictureUrl,
                contentDescription = "User Profile",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(GrayBlue.copy(alpha = 0.1f)),
                contentScale = ContentScale.Crop
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            AiMessageHeader()
            Spacer(modifier = Modifier.height(8.dp))

            AnimatedContent(
                targetState = message.isThinking,
                transitionSpec = {
                    if (targetState) {
                        fadeIn(animationSpec = tween(300))
                            .togetherWith(ExitTransition.None)
                    } else {
                        fadeIn(animationSpec = tween(300))
                            .togetherWith(fadeOut(animationSpec = tween(200)))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = "AiContentTransition"
            ) { thinking ->
                if (thinking) {
                    Text(
                        text = "Thinking...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = Gray,
                        lineHeight = 20.sp
                    )
                } else {
                    val isTypewriterActive by remember(progress, message.text) {
                        derivedStateOf { progress >= 0 && progress < message.text.length }
                    }
                    var delayedShowActionCard by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        TypewriterText(
                            text = message.text,
                            progress = progress,
                            isNewMessage = !isAlreadyAnimated,
                            onTextUpdate = onTextUpdate
                        )

                        val hasAction = message.action != null &&
                                message.action !is AiAgentAction.None &&
                                !message.actionExecuted &&
                                !message.actionCancelled &&
                                !message.isThinking

                        LaunchedEffect(isTypewriterActive, hasAction) {
                            if (!isTypewriterActive && hasAction) {
                                delay(250L)
                                delayedShowActionCard = true
                            } else {
                                delayedShowActionCard = false
                            }
                        }

                        LaunchedEffect(delayedShowActionCard) {
                            if (delayedShowActionCard) {
                                onTextUpdate()
                            }
                        }

                        AnimatedVisibility(
                            visible = delayedShowActionCard,
                            enter = fadeIn(animationSpec = tween(500)),
                            exit = fadeOut(animationSpec = tween(500))
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                ActionConfirmationCard(
                                    action = message.action!!,
                                    customers = customers,
                                    items = items,
                                    services = services,
                                    onConfirm = onConfirmAction,
                                    onCancel = onCancelAction
                                )
                            }
                        }

                        if (message.actionExecuted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Action executed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }

                        if (message.actionCancelled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Action cancelled",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiAgentPanelHeader(
    onClearHistory: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Aira",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            ),
            color = MainFontBlack
        )
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = MainFontBlack
                )
            }

            if (showMenu) {
                Popup(
                    alignment = Alignment.TopEnd,
                    offset = IntOffset(x = 0, y = 120),
                    onDismissRequest = { showMenu = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    Surface(
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(end = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 8.dp,
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                            Text(
                                text = "Delete History",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onClearHistory()
                                        showMenu = false
                                    }
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiAgentPanelInputArea(
    inputMessage: TextFieldValue,
    onInputChange: (TextFieldValue) -> Unit,
    onSendMessage: () -> Unit,
    modelStatus: AiModelStatus,
    currentModelName: String?,
    isProcessing: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Column {
            OutlinedTextField(
                value = inputMessage,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ask WashFlow AI...", color = Color.Gray) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Default
                ),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = GrayBlue
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MainFontBlack)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AnimatedContent(
                            targetState = modelStatus to currentModelName,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith
                                        fadeOut(animationSpec = tween(300))
                            },
                            label = "ModelIndicatorTransition"
                        ) { (status, name) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (status == AiModelStatus.IDLE) "Idle" else (name ?: ""),
                                    color = Gray,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                when (status) {
                                    AiModelStatus.IDLE -> {
                                        Icon(
                                            imageVector = Icons.Default.MoreHoriz,
                                            contentDescription = "Idle",
                                            modifier = Modifier.size(14.dp),
                                            tint = Gray
                                        )
                                    }
                                    AiModelStatus.THINKING -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 2.dp,
                                            color = GrayBlue
                                        )
                                    }
                                    AiModelStatus.SUCCESS -> {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Success",
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFF4CAF50)
                                        )
                                    }
                                    AiModelStatus.FAILURE -> {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Failed",
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.Red
                                        )
                                    }
                                    AiModelStatus.SWITCHING -> {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Switching",
                                            modifier = Modifier.size(14.dp),
                                            tint = GrayBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                    IconButton(
                        onClick = onSendMessage,
                        enabled = inputMessage.text.isNotBlank() && !isProcessing,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (inputMessage.text.isNotBlank() && !isProcessing)
                                    GrayBlue
                                else
                                    Color(0xFFE0E0E0)
                            )
                            .height(38.dp)
                            .width(52.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiAgentEmptyState(userName: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = "Hi, $userName",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = GrayBlue,
                fontSize = 32.sp
            )
        )
        Text(
            text = "What can I help you today?",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Medium,
                color = Gray,
                fontSize = 20.sp
            )
        )
        Spacer(modifier = Modifier.height(64.dp))

        // Info Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E2124))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "More ways to access AI",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Upgrade to a qualified Google AI plan for subscription access to Gemini, or provide API keys to use Anthropic, OpenAI, and Gemini via AI Studio. For offline development, run local models via local LLM hosts.",
                    color = Color(0xFFB0B0B0),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Prompts to try",
            fontWeight = FontWeight.Bold,
            color = MainFontBlack,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        PromptItem("Extract all hardcoded strings from this class and move them into strings.xml")
        PromptItem("Add documentation to my current file")
        PromptItem("Update kotlin in @libs.version.toml to the latest version")
        PromptItem("Make my Theme's color scheme warmer")

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AiAgentScrollToBottomButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xFF60B0FF).copy(alpha = 0.9f),
        // shadowElevation = 6.dp,
        border = BorderStroke(1.dp, Color(0xFFC1DFFF).copy(alpha = 0.9f)),
        modifier = modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Scroll to bottom",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
        }
    }
}

