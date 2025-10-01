package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.theme.GrayBlue

/**
 * Panel generik untuk mengedit atau menghapus data yang sudah ada.
 *
 * @param title Judul panel, biasanya nama item yang dipilih.
 * @param inputFields Daftar field yang bisa diedit.
 * @param onDoneClick Aksi saat tombol "Done" ditekan.
 * @param onDeleteClick Aksi saat tombol "Delete" ditekan.
 */
@Composable
fun EditDataPanel(
    title: String,
    inputFields: List<AddNewDataInputField>,
    onDoneClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White) // Latar belakang putih solid
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            inputFields.forEach { field ->
                OutlinedTextField(
                    value = field.value,
                    onValueChange = field.onValueChange,
                    label = { Text(field.label) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Baris untuk tombol Done dan Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tombol Delete
                OutlinedButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF44336)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(Color(0xFFF44336))
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Icon")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
                // Tombol Done
                Button(
                    onClick = onDoneClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GrayBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Done Icon")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Done")
                }
            }
        }
    }
}