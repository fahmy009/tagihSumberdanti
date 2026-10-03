package com.example.tagihsumberdanti.ui.admin

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tagihsumberdanti.ui.theme.DarkBackground
import com.example.tagihsumberdanti.ui.theme.LeafGreen
import com.example.tagihsumberdanti.ui.theme.SlateText

@Composable
fun AdminMainScreen(
    viewModel: AdminViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White.copy(alpha = 0.05f),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                    label = { Text("Beranda") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LeafGreen,
                        selectedTextColor = LeafGreen,
                        indicatorColor = LeafGreen.copy(alpha = 0.2f),
                        unselectedIconColor = SlateText,
                        unselectedTextColor = SlateText
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.People, contentDescription = "Pelanggan") },
                    label = { Text("Pelanggan") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LeafGreen,
                        selectedTextColor = LeafGreen,
                        indicatorColor = LeafGreen.copy(alpha = 0.2f),
                        unselectedIconColor = SlateText,
                        unselectedTextColor = SlateText
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Tagihan") },
                    label = { Text("Tagihan") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LeafGreen,
                        selectedTextColor = LeafGreen,
                        indicatorColor = LeafGreen.copy(alpha = 0.2f),
                        unselectedIconColor = SlateText,
                        unselectedTextColor = SlateText
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
                    label = { Text("Pengaturan") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LeafGreen,
                        selectedTextColor = LeafGreen,
                        indicatorColor = LeafGreen.copy(alpha = 0.2f),
                        unselectedIconColor = SlateText,
                        unselectedTextColor = SlateText
                    )
                )
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)
        when (selectedTab) {
            0 -> AdminDashboardScreen(viewModel = viewModel, modifier = screenModifier)
            1 -> PelangganScreen(viewModel = viewModel, modifier = screenModifier)
            2 -> TagihanScreen(viewModel = viewModel, modifier = screenModifier)
            3 -> PengaturanScreen(viewModel = viewModel, onLogout = onLogout, modifier = screenModifier)
        }
    }
}
