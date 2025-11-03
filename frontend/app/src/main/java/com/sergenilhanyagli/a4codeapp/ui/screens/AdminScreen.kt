package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(nav: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }
    var refreshTrigger by remember { mutableStateOf(0) } // ✅ yeni: ürün eklendiğinde tetiklenecek
    val tabs = listOf("Ürünler", "Çalışanlar", "Ciro", "Kategoriler")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("Admin Paneli") })
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { pad ->
        Box(modifier = Modifier.padding(pad).fillMaxSize()) {
            when (selectedTab) {
                0 -> ProductManagerScreen(nav) // ✅ tetikleyici
                1 -> EmployeeManagerScreen(nav)
                2 -> RevenueScreen(nav)
                3 -> CategoryManagerScreen(nav)
            }
        }
    }
}
