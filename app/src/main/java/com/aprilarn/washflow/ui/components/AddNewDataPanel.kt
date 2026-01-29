package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MainFontBlack


data class AddNewDataInputField(
    val value: String,
    val onValueChange: (String) -> Unit,
    val label: String
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
    val borderColor = Color.White

    Card(
        modifier = modifier
            .wrapContentHeight()
            .border(
                width = 2.dp,
                color = borderColor,
                shape = borderRadius
            ),
        shape = borderRadius,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .padding(vertical=24.dp, horizontal=22.dp),
            //horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Menggunakan title dari parameter
            // Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = GrayBlue
                )
            )
            Text(
                text = subTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = GrayBlue
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Membuat OutlinedTextField secara dinamis dari daftar inputFields
            inputFields.forEach { field ->
                OutlinedTextField(
                    value = field.value,
                    onValueChange = field.onValueChange,
                    label = { Text(field.label) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MainFontBlack,
                        unfocusedTextColor = Color.Gray,
                        cursorColor = Color.White,
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onAddClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GrayBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Icon")
                Spacer(modifier = Modifier.width(4.dp))
                // Menggunakan addButtonText dari parameter
                Text(addButtonText)
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
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.25f))
            ) {
                // Left panel content (e.g., CustomerListPanel)
            }

            val sampleFields = listOf(
                AddNewDataInputField(label = "Name", value = "", onValueChange = {}),
                AddNewDataInputField(label = "Contact", value = "", onValueChange = {})
            )
            AddNewDataPanel(
                title = "Add New Customer",
                subTitle = "Tambah pelanggan baru",
                inputFields = sampleFields,
                onAddClick = {},
                modifier = Modifier
                    .weight(1f)
            )
        }
    }

}