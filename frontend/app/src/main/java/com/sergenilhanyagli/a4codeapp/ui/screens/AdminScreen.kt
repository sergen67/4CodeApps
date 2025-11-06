package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.sergenilhanyagli.a4codeapp.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(nav: NavHostController, vm: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Ürünler", "Çalışanlar", "Ciro", "Kategoriler")

    Scaffold(
        containerColor = Color(0xFFF6F3FF),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Admin Paneli",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        // 🔹 Çıkış Butonu
                        TextButton(
                            onClick = {
                                vm.clearLoginState(context)
                                nav.navigate("login") {
                                    popUpTo(0)
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFFC1C1))
                        ) {
                            Text(
                                "Çıkış Yap",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF7B61FF))
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFFEDE7FF),
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    Box(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .padding(horizontal = 20.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF7B61FF))
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                color = if (selectedTab == index) Color(0xFF4A3AFF) else Color.Gray,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                when (selectedTab) {
                    0 -> ProductManagerScreen(nav)
                    1 -> EmployeeManagerScreen(nav)
                    2 -> RevenueScreen()
                    3 -> CategoryManagerScreen(nav)
                }
            }
        }
    }
}
