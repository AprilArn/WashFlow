package com.aprilarn.washflow.ui.phone.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aprilarn.washflow.AppNavigation
import com.aprilarn.washflow.ui.theme.GrayBlue

/**
 * Top Bar ringkas untuk HP biasa (Batang).
 * Menampilkan Brand WashFlow, Pemilih Workspace, Tombol AI Agent (tap = chat, long-press = voice), & Notifikasi.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhoneTopHeader(
    workspaceName: String,
    unreadCount: Int,
    isDarkBackground: Boolean,
    isAiVoiceActive: Boolean,
    onWorkspaceClick: () -> Unit,
    onNotifClick: () -> Unit,
    onAiAgentClick: () -> Unit,
    onAiAgentLongClick: () -> Unit,
    workspaceDropdown: @Composable (IntOffset) -> Unit,
    modifier: Modifier = Modifier
) {
    val brandAccentColor by animateColorAsState(
        targetValue = if (isDarkBackground) Color(0xFFD8E6FF) else Color(0xFF3B4870),
        animationSpec = tween(durationMillis = 500),
        label = "phoneHeaderBrandAccent"
    )
    var wsTriggerHeight by remember { mutableIntStateOf(0) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Kiri: Brand + Workspace Dropdown Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Wash",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = brandAccentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                )
                Text(
                    text = "Flow",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                )
            }

            Box(
                modifier = Modifier.onSizeChanged { wsTriggerHeight = it.height }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .widthIn(max = 165.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = if (isDarkBackground) 0.16f else 0.28f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(50)
                        )
                        .clickable { onWorkspaceClick() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = workspaceName.ifBlank { "Workspace" },
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Icon(
                        imageVector = Icons.Rounded.ArrowDropDown,
                        contentDescription = "Workspace Options",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                workspaceDropdown(IntOffset(0, wsTriggerHeight + 8))
            }
        }

        // Kanan: AI Agent + Notifikasi
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val aiBgColor = if (isAiVoiceActive) {
                Color(0xFF6366F1)
            } else {
                Color.White.copy(alpha = if (isDarkBackground) 0.18f else 0.30f)
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(aiBgColor)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .combinedClickable(
                        onClick = onAiAgentClick,
                        onLongClick = onAiAgentLongClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "AI Agent",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (isDarkBackground) 0.18f else 0.30f))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .clickable { onNotifClick() },
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(
                                containerColor = Color(0xFFEF4444),
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

/**
 * Bottom Navigation Bar ergonomis untuk HP biasa (Batang) agar mudah dijangkau satu tangan.
 */
@Composable
fun PhoneBottomNavigationBar(
    navController: NavController,
    isDarkBackground: Boolean = false,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("?")

    val items = remember {
        listOf(
            AppNavigation.Home,
            AppNavigation.Orders,
            AppNavigation.TableData,
            AppNavigation.Analytics,
            AppNavigation.Settings
        )
    }

    val isDataSubRoute = currentRoute == AppNavigation.Customers.route ||
            currentRoute == AppNavigation.Services.route ||
            currentRoute == AppNavigation.Items.route

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(26.dp),
        color = Color.White.copy(alpha = 0.94f),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val isSelected = currentRoute == screen.route ||
                        (screen == AppNavigation.TableData && isDataSubRoute) ||
                        (screen == AppNavigation.Home && currentRoute == AppNavigation.ManageOrder.route)

                val pillBgColor by animateColorAsState(
                    targetValue = if (isSelected) GrayBlue.copy(alpha = 0.12f) else Color.Transparent,
                    animationSpec = tween(200),
                    label = "phoneBottomNavBg_${screen.route}"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) GrayBlue else GrayBlue.copy(alpha = 0.55f),
                    animationSpec = tween(200),
                    label = "phoneBottomNavContent_${screen.route}"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(pillBgColor)
                        .clickable {
                            if (currentRoute != screen.route) {
                                val popped = navController.popBackStack(screen.route, inclusive = false)
                                if (!popped) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = false
                                        }
                                        launchSingleTop = true
                                        restoreState = false
                                    }
                                }
                            }
                        }
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        tint = contentColor,
                        modifier = Modifier.size(21.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = screen.label,
                        color = contentColor,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
