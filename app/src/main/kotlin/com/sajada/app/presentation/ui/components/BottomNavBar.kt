package com.sajada.app.presentation.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajada.app.presentation.viewmodel.Screen

val TealActive = Color(0xFF00897B)
val GrayInactive = Color(0xFF9E9E9E)

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.DASHBOARD, "Beranda", Icons.Default.Home),
    BottomNavItem(Screen.JADWAL, "Jadwal", Icons.Default.DateRange),
    BottomNavItem(Screen.ABSENSI, "Absensi", Icons.Default.CheckCircle),
    BottomNavItem(Screen.REKAP, "Rekap", Icons.Default.List),
    BottomNavItem(Screen.JAMAAH, "Jamaah", Icons.Default.Person)
)

@Composable
fun SajadaBottomNavBar(
    currentScreen: Screen,
    onSelect: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = GrayInactive,
        tonalElevation = 12.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentScreen == item.screen
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TealActive,
                    selectedTextColor = TealActive,
                    indicatorColor = TealActive.copy(alpha = 0.14f),
                    unselectedIconColor = GrayInactive,
                    unselectedTextColor = GrayInactive
                )
            )
        }
    }
}
