package com.aprilarn.washflow.ui.aiagent

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.ui.theme.Gray
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MainFontBlack
import com.aprilarn.washflow.ui.theme.SkyBlue
import com.aprilarn.washflow.utils.MarkdownUtils
import kotlinx.coroutines.delay

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
fun ActionConfirmationCard(
    action: AiAgentAction,
    customers: List<Customers> = emptyList(),
    onConfirm: (AiAgentAction?) -> Unit,
    onCancel: () -> Unit
) {
    var editedName by remember(action) {
        mutableStateOf(
            when (action) {
                is AiAgentAction.AddCustomer -> action.name
                is AiAgentAction.DeleteCustomer -> action.name
                else -> ""
            }
        )
    }
    var editedValue by remember(action) {
        mutableStateOf(
            when (action) {
                is AiAgentAction.AddCustomer -> action.phoneNumber
                is AiAgentAction.DeleteCustomer -> action.contact
                else -> ""
            }
        )
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

    // Auto-match for DeleteCustomer
    LaunchedEffect(action, customers) {
        if (action is AiAgentAction.DeleteCustomer && selectedCustomerId.isEmpty()) {
            // Prioritas pencarian:
            // 1. Exact match di Phone Number (Contact)
            // 2. Exact match di Name (Case Insensitive)
            // 3. Partial match di Name
            val match = customers.find { 
                action.contact.isNotBlank() && it.contact == action.contact 
            } ?: customers.find { 
                action.name.isNotBlank() && it.name.equals(action.name, ignoreCase = true)
            } ?: customers.find { 
                action.name.isNotBlank() && it.name.contains(action.name, ignoreCase = true)
            }

            if (match != null) {
                editedName = match.name
                editedValue = match.contact ?: ""
            }
        }
    }

    var isNameDropdownExpanded by remember { mutableStateOf(false) }
    var isPhoneDropdownExpanded by remember { mutableStateOf(false) }
    var nameTextFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var phoneTextFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

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
        is AiAgentAction.DeleteCustomer -> buildAnnotatedString {
            append("Delete customer ")
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
                text = "Confirmation required:",
                style = MaterialTheme.typography.labelSmall,
                color = Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (action is AiAgentAction.AddCustomer || action is AiAgentAction.DeleteCustomer) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box {
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { 
                                editedName = it
                                isNameDropdownExpanded = true
                                isPhoneDropdownExpanded = false
                            },
                            label = { Text("Customer Name", fontSize = 12.sp) },
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
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box {
                        OutlinedTextField(
                            value = editedValue,
                            onValueChange = { 
                                editedValue = it
                                if (action is AiAgentAction.DeleteCustomer) {
                                    isPhoneDropdownExpanded = true
                                    isNameDropdownExpanded = false
                                }
                            },
                            label = {
                                Text(
                                    if (action is AiAgentAction.AddCustomer || action is AiAgentAction.DeleteCustomer) "Phone Number" else "Customer ID",
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
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
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
                            else -> null
                        }
                        onConfirm(resultAction)
                    },
                    enabled = if (action is AiAgentAction.DeleteCustomer) selectedCustomerId.isNotEmpty() else true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (action is AiAgentAction.DeleteCustomer) Color(0xFFEF5350) else GrayBlue
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        if (action is AiAgentAction.DeleteCustomer) "Delete" else "Confirm", 
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
    modifier: Modifier = Modifier,
    delayMillis: Long = 20L,
    isNewMessage: Boolean = true,
    onTextUpdate: () -> Unit = {},
    onAnimationStateChange: (Boolean) -> Unit = {}
) {
    var displayedText by rememberSaveable {
        mutableStateOf(if (isNewMessage && text.isNotEmpty()) text.take(1) else text)
    }
    var animationFinished by rememberSaveable { mutableStateOf(!isNewMessage) }
    
    var lastProcessedText by rememberSaveable { mutableStateOf(text) }
    val view = LocalView.current

    LaunchedEffect(text) {
        if (text != lastProcessedText) {
            lastProcessedText = text
            if (isNewMessage) {
                displayedText = text.take(1)
                animationFinished = false
            } else {
                displayedText = text
                animationFinished = true
            }
        }

        if (!animationFinished) {
            onAnimationStateChange(true)
            if (text.isEmpty()) {
                displayedText = ""
            } else {
                val startIndex = displayedText.length
                for (index in startIndex until text.length) {
                    val currentChar = text[index]
                    displayedText = text.substring(0, index + 1)
                    
                    // Haptic feedback when a word is completed (on whitespace)
                    if (currentChar.isWhitespace()) {
                        val prevChar = if (index > 0) text[index - 1] else null
                        if (prevChar == null || !prevChar.isWhitespace()) {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        }
                    }

                    onTextUpdate()
                    delay(delayMillis)
                }
            }
            animationFinished = true
            onAnimationStateChange(false)
            // Much stronger/punchier haptic (REJECT usually provides a sharp triple-tap or strong kick)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        } else {
            displayedText = text
            onAnimationStateChange(false)
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
