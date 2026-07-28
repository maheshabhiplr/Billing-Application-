package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

sealed class Screen(val route: String, val titleMl: String, val icon: ImageVector) {
    object Billing : Screen("billing", "ബില്ലിംഗ്", Icons.Default.ReceiptLong)
    object DailyPrice : Screen("daily_price", "വിലപ്പട്ടിക", Icons.Default.PriceChange)
    object Customers : Screen("customers", "കസ്റ്റമേഴ്സ്", Icons.Default.People)
    object Products : Screen("products", "ഉൽപ്പന്നങ്ങൾ", Icons.Default.Category)
    object Invoices : Screen("invoices", "ബില്ലുകൾ", Icons.Default.History)
    object Company : Screen("company", "സ്ഥാപനം", Icons.Default.Store)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppStructure(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppStructure(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationItems = listOf(
        Screen.Billing,
        Screen.DailyPrice,
        Screen.Customers,
        Screen.Products,
        Screen.Invoices,
        Screen.Company
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                navigationItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.titleMl,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = screen.titleMl,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Billing.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Billing.route) {
                BillingScreen(
                    viewModel = viewModel,
                    onViewPdf = {
                        navController.navigate(Screen.Invoices.route)
                    }
                )
            }
            composable(Screen.DailyPrice.route) {
                DailyPriceEditorScreen(viewModel = viewModel)
            }
            composable(Screen.Customers.route) {
                CustomersScreen(viewModel = viewModel)
            }
            composable(Screen.Products.route) {
                ProductsScreen(viewModel = viewModel)
            }
            composable(Screen.Invoices.route) {
                InvoicesArchiveScreen(viewModel = viewModel)
            }
            composable(Screen.Company.route) {
                CompanySettingsScreen(viewModel = viewModel)
            }
        }
    }
}
