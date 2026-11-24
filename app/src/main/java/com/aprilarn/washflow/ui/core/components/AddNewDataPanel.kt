package com.aprilarn.washflow.ui.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.feature.masterdata.tabledata.resolveTableDataTheme
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText

data class AddNewDataInputField(
    val value: String,
    val onValueChange: (String) -> Unit,
    val label: String,
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default
)

@Composable
fun AddNewDataPanel(
    title: String,
    subTitle: String,
    inputFields: List<AddNewDataInputField>,
    onAddClick: () -> Unit,
    addButtonText: String = "Add",
    modifier: Modifier = Modifier
) {
    val borderRadius = RoundedCornerShape(24.dp)
    val theme = remember(title) { resolveTableDataTheme(title) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = borderRadius,
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
            // Editorial Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(32.dp)
                        .background(theme.accentColor, RoundedCornerShape(2.dp))
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = GrayBlue
                        )
                    )
                    Text(
                        text = subTitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PastelNeutralText,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = PastelNeutralBg, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic OutlinedTextFields
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                inputFields.forEach { field ->
                    OutlinedTextField(
                        value = field.value,
                        onValueChange = field.onValueChange,
                        label = { Text(field.label) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = field.keyboardOptions,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GrayBlue,
                            unfocusedTextColor = GrayBlue,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = theme.softBgColor,
                            focusedBorderColor = theme.accentColor,
                            unfocusedBorderColor = theme.badgeBgColor,
                            focusedLabelColor = theme.accentColor,
                            unfocusedLabelColor = PastelNeutralText,
                            cursorColor = theme.accentColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onAddClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.accentColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Icon",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = addButtonText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1200, heightDp = 600)
@Composable
fun AddNewDataPanelPreview() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxSize(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 6.dp
            ) {}

            val sampleFields = listOf(
                AddNewDataInputField(label = "Nama Pelanggan", value = "", onValueChange = {}),
                AddNewDataInputField(label = "No. WA", value = "", onValueChange = {})
            )
            AddNewDataPanel(
                title = "Add New Customer",
                subTitle = "Tambah pelanggan baru",
                inputFields = sampleFields,
                onAddClick = {},
                addButtonText = "Add Customer",
                modifier = Modifier.weight(1f)
            )
        }
    }
}
