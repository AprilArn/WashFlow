package com.aprilarn.washflow.ui.analytics.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.aprilarn.washflow.ui.theme.GrayBlue

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun GeneralDropdown(
    selectedText: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    dropdownWidth: Dp = 120.dp,
    popupAlignment: Alignment = Alignment.TopEnd,
    icon: ImageVector? = null,
    backgroundColor: Color = Color.White,
    shapeRadius: Dp = 12.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    onExpandedChange: ((Boolean) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var popTriggerSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    LaunchedEffect(expanded) {
        onExpandedChange?.invoke(expanded)
    }

    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(shapeRadius),
            color = backgroundColor,
            //border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
            modifier = Modifier
                .onGloballyPositioned { popTriggerSize = it.size }
                .clip(RoundedCornerShape(shapeRadius)) // Potong area ripple effect agar bulat
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = GrayBlue
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(text = selectedText, fontSize = 12.sp, color = GrayBlue)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = GrayBlue
                )
            }
        }

        if (expanded) {
            Popup(
                alignment = popupAlignment,
                offset = IntOffset(0, popTriggerSize.height),
                properties = PopupProperties(focusable = true),
                onDismissRequest = { expanded = false }
            ) {
                Surface(
                    modifier = Modifier
                        .width(dropdownWidth)
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        items.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onItemSelected(item)
                                        expanded = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item, fontSize = 14.sp)
                            }

                            if (index < items.size - 1) {
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}