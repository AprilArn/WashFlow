package com.aprilarn.washflow.ui.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aprilarn.washflow.ui.core.navigation.AppNavigation
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Menyimpan koordinat hasil pengukuran pada fase Layout untuk langsung dibaca pada fase Draw
 * di frame yang sama (Forward Flow: Layout -> Draw), tanpa memicu MutableState / Recomposition.
 */
private class NavTrackBoundsHolder(count: Int) {
    val lefts = FloatArray(count)
    val rights = FloatArray(count)
    var height = 0f
}

@Composable
fun NavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier,
    isDarkBackground: Boolean = false
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

    NavigationBarContent(
        items = items,
        selectedRoute = currentRoute,
        isDarkBackground = isDarkBackground,
        modifier = modifier
    ) { screen ->
        if (currentRoute != screen.route) {
            // Jika destination tab sudah ada di back stack aktif (mis. sedang di Customers di atas TableData,
            // atau ManageOrder di atas Home), pop langsung ke tab tersebut.
            val poppedToExistingTab = navController.popBackStack(screen.route, inclusive = false)

            if (!poppedToExistingTab) {
                // Navigasi langsung ke main screen tab tanpa menyimpan/me-restore sub-screen sebelumnya
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
}

@Composable
private fun NavigationBarContent(
    items: List<AppNavigation>,
    selectedRoute: String?,
    isDarkBackground: Boolean,
    modifier: Modifier = Modifier,
    onItemClick: (AppNavigation) -> Unit
) {
    val boundsHolder = remember(items.size) { NavTrackBoundsHolder(items.size) }

    // Animasi progress (0f..1f) per tab. Hanya dibaca pada fase Layout & Draw
    // sehingga sama sekali tidak memicu recomposition selama animasi berjalan.
    val progressStates: List<State<Float>> = items.map { screen ->
        val isSelected = selectedRoute == screen.route
        animateFloatAsState(
            targetValue = if (isSelected) 1f else 0f,
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "navProgress_${screen.route}"
        )
    }

    val containerBgColor by animateColorAsState(
        targetValue = if (isDarkBackground) {
            Color.White.copy(alpha = 0.14f)
        } else {
            Color.White.copy(alpha = 0.24f)
        },
        animationSpec = tween(durationMillis = 500),
        label = "navContainerBgColor"
    )

    val borderTopColor by animateColorAsState(
        targetValue = if (isDarkBackground) {
            Color.White.copy(alpha = 0.36f)
        } else {
            Color.White.copy(alpha = 0.58f)
        },
        animationSpec = tween(durationMillis = 500),
        label = "navBorderTopColor"
    )

    val borderBottomColor by animateColorAsState(
        targetValue = if (isDarkBackground) {
            Color.White.copy(alpha = 0.12f)
        } else {
            Color.White.copy(alpha = 0.24f)
        },
        animationSpec = tween(durationMillis = 500),
        label = "navBorderBottomColor"
    )

    val dockShape = RoundedCornerShape(50)
    val pillBrush = remember {
        Brush.verticalGradient(
            colors = listOf(Color.White, Color(0xFFF6F9FF))
        )
    }
    val pillShadowColor = remember { GrayBlue.copy(alpha = 0.16f) }

    Layout(
        modifier = modifier
            .wrapContentWidth()
            .clip(dockShape)
            .background(containerBgColor)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(borderTopColor, borderBottomColor)
                ),
                shape = dockShape
            )
            .padding(horizontal = 6.dp, vertical = 5.dp)
            .drawBehind {
                // Fase Draw: Menghitung posisi kapsul aktif dari bobot progress tiap tab.
                // Berjalan setelah fase Layout di frame yang sama tanpa recomposition.
                var totalWeight = 0f
                var weightedLeft = 0f
                var weightedRight = 0f
                var maxSingleWeight = 0f

                for (i in progressStates.indices) {
                    val w = progressStates[i].value.coerceAtLeast(0f)
                    if (w > 0.0001f) {
                        totalWeight += w
                        weightedLeft += w * boundsHolder.lefts[i]
                        weightedRight += w * boundsHolder.rights[i]
                        if (w > maxSingleWeight) {
                            maxSingleWeight = w
                        }
                    }
                }

                if (totalWeight > 0.0001f && boundsHolder.height > 0f) {
                    val pillAlpha = totalWeight.coerceIn(0f, 1f)
                    val normMax = (maxSingleWeight / totalWeight).coerceIn(0f, 1f)
                    // Efek liquid stretch halus saat kapsul sedang meluncur di antara dua tab
                    val midAirStretch = (1f - normMax) * 2f * 5.dp.toPx()

                    val pillLeft = (weightedLeft / totalWeight) - midAirStretch
                    val pillRight = (weightedRight / totalWeight) + midAirStretch
                    val pillWidth = (pillRight - pillLeft).coerceAtLeast(0f)
                    val pillHeight = boundsHolder.height
                    val cornerRadius = CornerRadius(pillHeight / 2f, pillHeight / 2f)

                    // Soft drop shadow di bawah kapsul putih
                    drawRoundRect(
                        color = pillShadowColor,
                        topLeft = Offset(pillLeft, 2.dp.toPx()),
                        size = Size(pillWidth, pillHeight),
                        cornerRadius = cornerRadius,
                        alpha = pillAlpha
                    )

                    // Kapsul putih aktif (ikut memudar halus saat masuk ke sub-halaman tanpa tab aktif)
                    drawRoundRect(
                        brush = pillBrush,
                        topLeft = Offset(pillLeft, 0f),
                        size = Size(pillWidth, pillHeight),
                        cornerRadius = cornerRadius,
                        alpha = pillAlpha
                    )
                }
            },
        content = {
            items.forEachIndexed { index, screen ->
                val isSelected = selectedRoute == screen.route
                NavItem(
                    icon = screen.icon,
                    label = screen.label,
                    route = screen.route,
                    isSelected = isSelected,
                    selectionProgressState = progressStates[index],
                    isDarkBackground = isDarkBackground,
                    onClick = { onItemClick(screen) }
                )
            }
        }
    ) { measurables, constraints ->
        val spacingPx = 4.dp.roundToPx()
        val itemConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { it.measure(itemConstraints) }

        var currentX = 0
        var maxHeight = 0
        placeables.forEachIndexed { index, placeable ->
            boundsHolder.lefts[index] = currentX.toFloat()
            boundsHolder.rights[index] = (currentX + placeable.width).toFloat()
            currentX += placeable.width + if (index < placeables.lastIndex) spacingPx else 0
            maxHeight = max(maxHeight, placeable.height)
        }
        boundsHolder.height = maxHeight.toFloat()

        layout(width = currentX, height = maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val y = (maxHeight - placeable.height) / 2
                placeable.placeRelative(boundsHolder.lefts[index].roundToInt(), y)
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    route: String,
    isSelected: Boolean,
    selectionProgressState: State<Float>,
    isDarkBackground: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val unselectedContentColor = if (isDarkBackground) {
        Color.White.copy(alpha = 0.88f)
    } else {
        Color.White.copy(alpha = 0.92f)
    }

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) GrayBlue else unselectedContentColor,
        animationSpec = tween(durationMillis = 220),
        label = "contentColor_$route"
    )

    val itemPressScaleState = animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "itemPressScale_$route"
    )

    val iconRotationState = animateFloatAsState(
        targetValue = if (isSelected && route == AppNavigation.Settings.route) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconRotation_$route"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .graphicsLayer {
                val pressScale = itemPressScaleState.value
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(50))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer {
                    // Fase Draw: Skala & rotasi ikon dihitung langsung tanpa recomposition
                    val progress = selectionProgressState.value.coerceIn(0f, 1f)
                    val scale = 1f + (0.12f * progress)
                    scaleX = scale
                    scaleY = scale
                    rotationZ = iconRotationState.value
                }
        )

        // Label selalu berada di pohon komposisi (menghindari overhead mount/unmount AnimatedVisibility).
        // Ekspansi lebar dieksekusi murni di fase Layout dan fade/slide di fase Draw.
        Text(
            text = label,
            color = contentColor,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.15.sp,
            maxLines = 1,
            modifier = Modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints.copy(minWidth = 0))
                    val progress = selectionProgressState.value.coerceIn(0f, 1f)
                    val spacerPx = 8.dp.roundToPx()
                    val totalTargetWidth = spacerPx + placeable.width
                    val currentWidth = (totalTargetWidth * progress).roundToInt()

                    layout(width = currentWidth, height = placeable.height) {
                        if (currentWidth > 0) {
                            val slideOffset = ((1f - progress) * -(placeable.width / 4f)).roundToInt()
                            placeable.placeRelative(x = spacerPx + slideOffset, y = 0)
                        }
                    }
                }
                .graphicsLayer {
                    val progress = selectionProgressState.value.coerceIn(0f, 1f)
                    alpha = ((progress - 0.15f) / 0.85f).coerceIn(0f, 1f)
                }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB9E9FF)
@Composable
fun NavigationBarPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        NavigationBar(navController = rememberNavController())
    }
}

@Preview(name = "Data Selected", showBackground = true, backgroundColor = 0xFFB9E9FF)
@Composable
fun NavigationBarDataSelectedPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        NavigationBarContent(
            items = listOf(
                AppNavigation.Home,
                AppNavigation.Orders,
                AppNavigation.TableData,
                AppNavigation.Analytics,
                AppNavigation.Settings
            ),
            selectedRoute = AppNavigation.TableData.route,
            isDarkBackground = false,
            onItemClick = {}
        )
    }
}


