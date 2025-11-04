package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(nav: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Ürünler", "Çalışanlar", "Ciro", "Kategoriler")

    Scaffold(
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF9C8DF5),
                                Color(0xFFBBA9F6),
                                Color(0xFFEDEAFF)
                            )
                        )
                    )
                    .padding(bottom = 8.dp)
            ) {
                // 🔹 Başlık
                Text(
                    text = "Admin Paneli",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(16.dp)
                )

                // 🔹 Sekme Menüsü
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 12.dp,
                    indicator = { tabPositions ->
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .padding(horizontal = 20.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.8f))
                        )
                    },
                    divider = {},
                    containerColor = Color.Transparent
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            modifier = Modifier.padding(vertical = 4.dp),
                            text = {
                                Text(
                                    text = title,
                                    color = if (selectedTab == index)
                                        Color.White else Color(0xFF4B3E82),
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad)) {
            when (selectedTab) {
                0 -> ProductManagerScreen(nav)
                1 -> EmployeeManagerScreen(nav)
                2 -> RevenueScreen(nav)
                3 -> CategoryManagerScreen(nav)
            }
        }
    }
}

