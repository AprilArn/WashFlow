package com.aprilarn.washflow.ui.aiagent

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.theme.Gray
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MainFontBlack

@Composable
fun AiAgentPanel(
    expanded: Boolean,
    userName: String,
    inputMessage: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onDismiss() }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(400.dp),
                color = Color.White,
                shadowElevation = 24.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WashFlow AI",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            ),
                            color = MainFontBlack
                        )
                        Box(
                            modifier = Modifier.height(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "0",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Normal
                                ),
                                color = MainFontBlack
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 24.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp)
                            ) {
                                Spacer(modifier = Modifier.height(24.dp))
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
                                Spacer(modifier = Modifier.height(32.dp))

                                // Info Card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1E2124)) // Dark background from image
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
                            }
                        }
                    }

                    // Input Area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
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
                                        TextButton(
                                            onClick = {},
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Default", color = Gray, fontSize = 12.sp)
                                                CustomIcon(Icons.Default.KeyboardArrowDown, contentDescription = null, size = 12.dp, tint = Gray)
                                            }
                                        }
                                        IconButton(
                                            onClick = onSendMessage,
                                            enabled = inputMessage.isNotBlank(),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (inputMessage.isNotBlank()) GrayBlue else Color(0xFFE0E0E0))
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
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
//                            Box(
//                                modifier = Modifier
//                                    .clip(RoundedCornerShape(4.dp))
//                                    .background(GrayBlue)
//                                    .padding(horizontal = 4.dp, vertical = 2.dp)
//                            ) {
//                                Text("AI Pro", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
//                            }
//                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "AI can make mistakes, so double-check it",
                                color = Gray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
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
private fun CustomIcon(imageVector: ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier.size(size),
        tint = tint
    )
}

@Preview(showBackground = true)
@Composable
fun AiAgentPanelPreview() {
    AiAgentPanel(
        expanded = true,
        userName = "April",
        inputMessage = "",
        onInputChange = {},
        onSendMessage = {},
        onDismiss = {}
    )
}
